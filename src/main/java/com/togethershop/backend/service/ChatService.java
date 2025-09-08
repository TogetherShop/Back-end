package com.togethershop.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.ChatMessage;
import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.domain.CouponProposal;
import com.togethershop.backend.dto.ChatMessageDTO;
import com.togethershop.backend.dto.CouponDTO;
import com.togethershop.backend.dto.MessageType;
import com.togethershop.backend.repository.ChatMessageRepository;
import com.togethershop.backend.repository.ChatRoomRepository;
import com.togethershop.backend.repository.CouponProposalRepository;
import com.togethershop.backend.repository.ShopUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatMessageRepository messageRepo;
    private final ChatRoomRepository roomRepo;
    private final CouponProposalRepository proposalRepo;
    private final CustomerCouponService customerCouponService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ShopUserRepository userRepo;

    private final RedisTemplate<String, String> redisTemplate; // Redis 저장용
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환용

    @Transactional
    public ChatMessage sendTextMessage(String roomId, String senderUsername, String text) {
        ChatRoom room = roomRepo.findByRoomId(roomId).orElseThrow(IllegalArgumentException::new);
        Business sender = userRepo.findByUsername(senderUsername).orElseThrow(IllegalArgumentException::new);

        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(sender.getId())
                .type(MessageType.TEXT)
                .content(text)
                .sentAt(LocalDateTime.now())
                .build();
        msg = messageRepo.save(msg);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, messageToDto(msg));
        return msg;
    }

    @Transactional
    public ChatMessageDTO save(ChatMessageDTO dto) throws JsonProcessingException {
        ChatRoom room = roomRepo.findByRoomId(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Business sender = userRepo.findByUsername(dto.getSenderName())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        ChatMessage msg;
        if (dto.getMessageType() == null || dto.getMessageType().equals(MessageType.TEXT)) {
            // 일반 텍스트 메시지
            msg = ChatMessage.builder()
                    .room(room)
                    .senderId(sender.getId())
                    .type(MessageType.TEXT)
                    .content(dto.getContent())
                    .sentAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                    .build();

            msg = messageRepo.save(msg);

            // WebSocket 전송
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), messageToDto(msg));

        } else if (dto.getMessageType().equals(MessageType.COUPON_PROPOSAL)) {
            // 제안 메시지 처리
            // DTO content에 JSON 형태로 discountPercent, totalQty, startDate, endDate 들어왔다고 가정
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> proposalData = mapper.readValue(dto.getContent(), Map.class);

            Long discountPercent = (Long) proposalData.get("discountPercent");
            Integer totalQty = (Integer) proposalData.get("totalQuantity");
            LocalDate start = LocalDate.parse((String) proposalData.get("startDate"));
            LocalDate end = LocalDate.parse((String) proposalData.get("endDate"));

            CouponProposal proposal = CouponProposal.builder()
                    .room(room)
                    .businessId(sender.getId())
                    .discountValue(discountPercent)
                    .totalQuantity(totalQty)
                    .startDate(start)
                    .endDate(end)
                    .acceptedByRequester(false)
                    .acceptedByRecipient(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            proposal = proposalRepo.save(proposal);

            // 메시지 저장
            msg = ChatMessage.builder()
                    .room(room)
                    .senderId(sender.getId())
                    .type(MessageType.COUPON_PROPOSAL)
                    .content(dto.getContent())
                    .sentAt(LocalDateTime.now())
                    .build();
            messageRepo.save(msg);

            // WebSocket 전송
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), Map.of(
                    "type", "PROPOSAL",
                    "payload", proposalData,
                    "createdAt", msg.getSentAt()
            ));
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + dto.getMessageType());
        }

        return convertToDTO(msg);
    }

    @Transactional
    public void proposeCoupon(String roomId, String senderUsername,
                              CouponDTO proposerCoupon, CouponDTO recipientCoupon) throws JsonProcessingException {

        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        Business sender = userRepo.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderUsername));

        Map<String, Object> proposalPayload = Map.of(
                "roomId", roomId,
                "proposerId", sender.getId(),
                "proposerName", sender.getUsername(),
                "createdAt", System.currentTimeMillis(),
                "proposerCoupon", Map.of(
                        "discountPercent", proposerCoupon.getDiscountPercent(),
                        "totalQuantity", proposerCoupon.getTotalQuantity(),
                        "startDate", proposerCoupon.getStartDate(),
                        "endDate", proposerCoupon.getEndDate(),
                        "itemName", proposerCoupon.getItemName()
                ),
                "recipientCoupon", Map.of(
                        "discountPercent", recipientCoupon.getDiscountPercent(),
                        "totalQuantity", recipientCoupon.getTotalQuantity(),
                        "startDate", recipientCoupon.getStartDate(),
                        "endDate", recipientCoupon.getEndDate(),
                        "itemName", recipientCoupon.getItemName()
                )
        );

        String key = "proposal:" + roomId;
        String proposalJson = objectMapper.writeValueAsString(proposalPayload);

        // 30분 후 자동 만료 설정
        redisTemplate.opsForValue().set(key, proposalJson, Duration.ofMinutes(30));

        // WebSocket 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "PROPOSAL",
                "payload", proposalPayload,
                "timestamp", System.currentTimeMillis(),
                "expiresAt", System.currentTimeMillis() + Duration.ofMinutes(30).toMillis()
        ));

        log.info("쿠폰 제안 저장 및 전송 완료 - roomId={}, proposerId={}, expiresIn=30min",
                roomId, sender.getId());
    }

    @Transactional
    public void acceptProposal(String roomId, Long accepterId) throws JsonProcessingException {
        String key = "proposal:" + roomId;

        // 원자적으로 가져오고 삭제 (GETDEL 명령어 사용)
        String proposalJson = redisTemplate.opsForValue().getAndDelete(key);

        if (proposalJson == null) {
            log.warn("제안을 찾을 수 없거나 이미 처리됨 - roomId={}, accepterId={}", roomId, accepterId);
            throw new IllegalArgumentException("Proposal not found or already processed for roomId=" + roomId);
        }

        try {
            Map<String, Object> proposalPayload = objectMapper.readValue(proposalJson, Map.class);
            Long proposerId = Long.valueOf(proposalPayload.get("proposerId").toString());

            // 자신의 제안은 수락할 수 없음
            if (proposerId.equals(accepterId)) {
                log.warn("자신의 제안 수락 시도 - roomId={}, userId={}", roomId, accepterId);
                throw new IllegalArgumentException("Cannot accept your own proposal");
            }

            log.info("제안 수락 처리 시작 - roomId={}, proposerId={}, accepterId={}", roomId, proposerId, accepterId);

            // 쿠폰 발급
            customerCouponService.issueMutualCoupons(roomId, proposerId, accepterId, proposalPayload);

            // WebSocket 전송
            Map<String, Object> response = Map.of(
                    "type", "PROPOSAL_ACCEPTED",
                    "payload", proposalPayload,
                    "acceptedBy", accepterId,
                    "timestamp", System.currentTimeMillis()
            );

            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);

            log.info("제안 수락 완료 - roomId={}, proposerId={}, accepterId={}", roomId, proposerId, accepterId);

        } catch (JsonProcessingException e) {
            log.error("제안 데이터 파싱 실패 - roomId={}", roomId, e);
            throw new IllegalArgumentException("Invalid proposal data format");
        } catch (Exception e) {
            log.error("제안 수락 처리 중 오류 - roomId={}, accepterId={}", roomId, accepterId, e);
            throw e;
        }
    }

    @Transactional
    public void rejectProposal(String roomId, Long rejecterId, String reason) {
        String key = "proposal:" + roomId;

        // 원자적으로 가져오고 삭제
        String proposalJson = redisTemplate.opsForValue().getAndDelete(key);

        if (proposalJson == null) {
            log.warn("거절할 제안을 찾을 수 없음 - roomId={}, rejecterId={}", roomId, rejecterId);
            throw new IllegalArgumentException("Proposal not found or already processed for roomId=" + roomId);
        }

        try {
            Map<String, Object> proposalPayload = objectMapper.readValue(proposalJson, Map.class);
            Long proposerId = Long.valueOf(proposalPayload.get("proposerId").toString());

            // WebSocket 전송
            Map<String, Object> response = Map.of(
                    "type", "PROPOSAL_REJECTED",
                    "proposerId", proposerId,
                    "rejectedBy", rejecterId,
                    "reason", reason != null ? reason : "사유 없음",
                    "timestamp", System.currentTimeMillis()
            );

            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);

            log.info("제안 거절 완료 - roomId={}, proposerId={}, rejecterId={}", roomId, proposerId, rejecterId);

        } catch (JsonProcessingException e) {
            log.error("제안 데이터 파싱 실패 - roomId={}", roomId, e);
            // 이미 삭제했으므로 로그만 남김
        }
    }

    public Page<ChatMessageDTO> history(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> result = messageRepo.findByRoomRoomIdOrderBySentAtAsc(roomId, pageable);
        return result.map(this::convertToDTO);
    }

    private ChatMessageDTO convertToDTO(ChatMessage entity) {
        Business sender = userRepo.findById(entity.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        ChatMessageDTO.ChatMessageDTOBuilder builder = ChatMessageDTO.builder()
                .roomId(entity.getRoom().getRoomId())
                .senderId(entity.getSenderId())
                .senderName(sender.getUsername())
                .shopName(sender.getBusinessName())
                .messageType(entity.getType())
                .createdAt(entity.getSentAt());

        // PROPOSAL 타입 메시지의 경우 payload 파싱
        if (entity.getType() == MessageType.COUPON_PROPOSAL) {
            try {
                // content에 저장된 JSON을 Map으로 파싱
                Map<String, Object> payload = objectMapper.readValue(entity.getContent(), Map.class);
                builder.content("쿠폰 교환 제안")  // 사용자 친화적인 메시지
                        .payload(payload);  // payload 설정
            } catch (JsonProcessingException e) {
                log.error("PROPOSAL 메시지 payload 파싱 실패: {}", entity.getContent(), e);
                builder.content("쿠폰 교환 제안 (파싱 오류)");
            }
        } else {
            builder.content(entity.getContent());
        }

        return builder.build();
    }

    // WebSocket 전송용 메시지 변환
    private Map<String, Object> messageToDto(ChatMessage m) {
        Business sender = userRepo.findById(m.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", m.getId());
        result.put("type", m.getType().name());
        result.put("senderId", m.getSenderId());
        result.put("senderName", sender.getUsername());
        result.put("createdAt", m.getSentAt());
        result.put("timestamp", System.currentTimeMillis());

        // PROPOSAL 타입의 경우 payload 추가
        if (m.getType() == MessageType.COUPON_PROPOSAL) {
            try {
                Map<String, Object> payload = objectMapper.readValue(m.getContent(), Map.class);
                result.put("content", "쿠폰 교환 제안");
                result.put("payload", payload);
            } catch (JsonProcessingException e) {
                log.error("PROPOSAL 메시지 payload 파싱 실패", e);
                result.put("content", "쿠폰 교환 제안 (파싱 오류)");
            }
        } else {
            result.put("content", m.getContent());
        }

        return result;
    }

    public Map<String, Object> getProposalStatus(String roomId) {
        String key = "proposal:" + roomId;
        String proposalJson = redisTemplate.opsForValue().get(key);

        if (proposalJson == null) {
            return Map.of(
                    "exists", false,
                    "message", "현재 진행 중인 제안이 없습니다."
            );
        }

        try {
            Map<String, Object> proposalData = objectMapper.readValue(proposalJson, Map.class);
            Long createdAt = Long.valueOf(proposalData.get("createdAt").toString());
            Long remainingTime = createdAt + Duration.ofMinutes(30).toMillis() - System.currentTimeMillis();

            return Map.of(
                    "exists", true,
                    "proposalData", proposalData,
                    "remainingTimeMs", Math.max(0, remainingTime),
                    "isExpired", remainingTime <= 0
            );
        } catch (JsonProcessingException e) {
            log.error("제안 데이터 파싱 실패 - roomId: {}", roomId, e);
            return Map.of(
                    "exists", false,
                    "message", "제안 데이터 오류"
            );
        }
    }
}
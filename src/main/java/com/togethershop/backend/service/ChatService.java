package com.togethershop.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.togethershop.backend.domain.*;
import com.togethershop.backend.dto.*;
import com.togethershop.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository messageRepo;
    private final ChatRoomRepository roomRepo;
    private final CouponTemplateRepository templateRepo;
    private final CouponService couponService;
    private final ShopUserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final PartnershipRepository partnershipRepo;
    private final ObjectMapper objectMapper;
    private final RedisChatPublisher redisChatPublisher;

    // 1️⃣ 일반 텍스트 메시지 전송
    @Transactional
    public ChatMessage sendTextMessage(String roomId, Long senderId, String text) {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Business sender = userRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        Partnership p = partnershipRepo.findByRequester_IdOrPartner_Id(room.getRequester().getId(), room.getRecipient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Partnership not found"));
        Business receiver = p.getRequester().getId().equals(sender.getId()) ? p.getPartner() : p.getRequester();
        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(sender.getId())
                .receiverBusinessId(receiver.getId())
                .partnership(room.getPartnership())
                .type(MessageType.TEXT)
                .content(text)
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        msg = messageRepo.save(msg);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, messageToDto(msg));
        return msg;
    }

    @Transactional
    public ChatMessageDTO proposeCoupon(String roomId, Long senderId, CouponTemplate couponTemplate) throws Exception {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Business sender = userRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        if (couponTemplate == null) {
            throw new IllegalArgumentException("proposerCoupon 정보가 필요합니다");
        }

        // DB 저장
        templateRepo.save(couponTemplate);
        log.info("CouponTemplate 저장 완료, ID: {}", couponTemplate.getId());

        // 프론트와 동일한 구조로 ProposalPayloadDTO 생성
        ProposalPayloadDTO payload = new ProposalPayloadDTO();
        payload.setRoomId(room.getRoomId());
        payload.setProposerId(sender.getId());

        // proposerCoupon 세팅
        CouponDTO proposerPayload = new CouponDTO();
        proposerPayload.setItemName(couponTemplate.getDescription());
        proposerPayload.setDiscountValue(couponTemplate.getDiscountValue().intValue());
        proposerPayload.setTotalQuantity(couponTemplate.getTotalQuantity());
        proposerPayload.setStartDate(couponTemplate.getStartDate());
        proposerPayload.setEndDate(couponTemplate.getEndDate());
        payload.setProposerCoupon(proposerPayload);

        // recipientCoupon은 일단 같은 값으로 세팅 (필요 시 수정)
        CouponDTO recipientPayload = new CouponDTO();
        recipientPayload.setItemName(couponTemplate.getDescription());
        recipientPayload.setDiscountValue(couponTemplate.getDiscountValue().intValue());
        recipientPayload.setTotalQuantity(couponTemplate.getTotalQuantity());
        recipientPayload.setStartDate(couponTemplate.getStartDate());
        recipientPayload.setEndDate(couponTemplate.getEndDate());
        payload.setRecipientCoupon(recipientPayload);

        payload.setStatus("WAITING");

        // 메시지 생성
        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(sender.getId())
                .type(MessageType.COUPON_PROPOSAL)
                .partnership(room.getPartnership())
                .content(objectMapper.writeValueAsString(payload)) // JSON 저장
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .receiverBusinessId(room.getRecipient().getId())
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(msg);

        ChatMessageDTO chatMessageDTO = convertToDTO(msg);

        // 보낸 사람 (버튼 없음)
        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/topic/room", Map.of(
                "message", chatMessageDTO,
                "showActionButtons", false
        ));

        // 받는 사람 (버튼 있음)
        String recipientUsername = room.getPartnership().getPartner().getUsername();
        messagingTemplate.convertAndSendToUser(recipientUsername, "/topic/room", Map.of(
                "message", chatMessageDTO,
                "showActionButtons", true
        ));

        redisChatPublisher.publish(chatMessageDTO);

        return chatMessageDTO;
    }


    // 3️⃣ 제안 수락
    @Transactional
    public void acceptProposal(Long proposalId, Long accepterId) throws Exception {
        CouponTemplate proposal = templateRepo.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (proposal.getBusinessId().equals(accepterId)) {
            throw new IllegalArgumentException("Cannot accept your own proposal");
        }

        proposal.setAcceptedByRecipient(true);
        templateRepo.save(proposal);

        // 쿠폰 발급
        CouponIssueRequestDTO dto = CouponIssueRequestDTO.builder()
                .businessId(proposal.getBusinessId())
                .couponCode("AUTO_" + UUID.randomUUID())
                .expiredDate(proposal.getEndDate().atStartOfDay())
                .build();
        couponService.issueCoupon(dto);

        messagingTemplate.convertAndSend("/topic/room/" + proposal.getRoom().getRoomId(), Map.of(
                "type", "PROPOSAL_ACCEPTED",
                "proposalId", proposalId,
                "acceptedBy", accepterId,
                "timestamp", System.currentTimeMillis()
        ));
    }

    // 4️⃣ 제안 거절
    @Transactional
    public void rejectProposal(Long proposalId, Long rejecterId, String reason) {
        CouponTemplate proposal = templateRepo.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));


        templateRepo.save(proposal);

        messagingTemplate.convertAndSend("/topic/room/" + proposal.getRoom().getRoomId(), Map.of(
                "type", "PROPOSAL_REJECTED",
                "proposalId", proposalId,
                "rejectedBy", rejecterId,
                "reason", reason != null ? reason : "사유 없음",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> propose(String roomId) {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // 해당 방의 활성 쿠폰 제안 조회
        List<CouponTemplate> activeProposals = templateRepo.findByRoom(room);

        List<Map<String, Object>> proposals = activeProposals.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("proposalId", p.getId());
            map.put("description", p.getDescription());
            map.put("discountValue", p.getDiscountValue());
            map.put("totalQuantity", p.getTotalQuantity());
            map.put("startDate", p.getStartDate());
            map.put("endDate", p.getEndDate());
            map.put("acceptedByRequester", p.isAcceptedByRequester());
            map.put("acceptedByRecipient", p.isAcceptedByRecipient());
            return map;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("roomId", roomId);
        result.put("proposals", proposals);
        return result;
    }

    // 5️⃣ 채팅 히스토리 조회
    public Page<ChatMessageDTO> history(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> result = messageRepo.findByRoomRoomIdOrderBySentAtAsc(roomId, pageable);
        return result.map(this::convertToDTO);
    }

    // DTO 변환
    private ChatMessageDTO convertToDTO(ChatMessage entity) {
        Business sender = userRepo.findById(entity.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        ChatMessageDTO.ChatMessageDTOBuilder builder = ChatMessageDTO.builder()
                .roomId(entity.getRoom().getRoomId())
                .senderId(entity.getSenderId())
                .senderName(sender.getUsername())
                .businessName(sender.getBusinessName())
                .type(entity.getType())
                .timestamp(entity.getSentAt());

        if (entity.getType() == MessageType.COUPON_PROPOSAL) {
            try {
                // ✅ JSON → ProposalPayloadDTO로 변환
                ProposalPayloadDTO payload = objectMapper.readValue(entity.getContent(), ProposalPayloadDTO.class);
                builder.content("쿠폰 교환 제안")
                        .payload(payload);
            } catch (Exception e) {
                builder.content("쿠폰 교환 제안 (파싱 오류)");
            }
        } else {
            builder.content(entity.getContent());
        }

        return builder.build();
    }


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

        if (m.getType() == MessageType.COUPON_PROPOSAL) {
            try {
                Map<String, Object> payload = objectMapper.readValue(m.getContent(), Map.class);
                result.put("content", "쿠폰 교환 제안");
                result.put("payload", payload);
            } catch (Exception e) {
                result.put("content", "쿠폰 교환 제안 (파싱 오류)");
            }
        } else {
            result.put("content", m.getContent());
        }

        return result;
    }
}

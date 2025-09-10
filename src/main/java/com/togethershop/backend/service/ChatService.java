package com.togethershop.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository messageRepo;
    private final ChatRoomRepository roomRepo;
    private final CouponTemplateRepository templateRepo;
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

        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(sender.getId())
                .receiverBusinessId(room.getRecipient().getId())
                .partnership(room.getPartnership())
                .type(MessageType.TEXT)
                .content(text)
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .sentAt(Instant.now())
                .build();

        msg = messageRepo.save(msg);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, convertToDTO(msg));
        return msg;
    }

    @Transactional
    public ChatMessageDTO proposeCoupon(String roomId, Long senderId, ProposalPayloadDTO coupon) throws Exception {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Business sender = userRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        if (coupon == null) {
            throw new IllegalArgumentException("proposerCoupon 정보가 필요합니다");
        }

        // ✅ proposal payload만 저장
        ProposalPayloadDTO payload = new ProposalPayloadDTO();
        payload.setRoomId(room.getRoomId());
        payload.setProposerId(sender.getId());
        payload.setProposerCoupon(coupon.getProposerCoupon());
        payload.setRecipientCoupon(coupon.getRecipientCoupon());
        payload.setStatus("REQUESTED");

        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(sender.getId())
                .type(MessageType.COUPON_PROPOSAL)
                .partnership(room.getPartnership())
                .content(objectMapper.writeValueAsString(payload)) // JSON
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .receiverBusinessId(room.getRecipient().getId())
                .sentAt(Instant.now())
                .build();

        messageRepo.save(msg);
        System.out.println(convertToDTO(msg));
        messagingTemplate.convertAndSend("/topic/room/" + roomId, convertToDTO(msg));
        return convertToDTO(msg);
    }


    // 3️⃣ 제안 수락
    @Transactional
    public void acceptProposal(Long messageId) throws Exception {
        ChatMessage proposalMessage = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal message not found"));

        ProposalPayloadDTO payload = objectMapper.readValue(proposalMessage.getContent(), ProposalPayloadDTO.class);
        payload.setStatus("ACCEPTED");
        proposalMessage.setContent(objectMapper.writeValueAsString(payload));
        messageRepo.save(proposalMessage);
        ChatRoom room = proposalMessage.getRoom();


        Business proposer = userRepo.findById(payload.getProposerId())
                .orElseThrow(() -> new IllegalArgumentException("Proposer not found"));

        Business recipient = userRepo.findById(
                Objects.equals(room.getRequester().getId(), proposer.getId()) ? room.getRecipient().getId() : room.getRequester().getId()
        ).orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // ✅ 로그 추가
        log.info("acceptProposal 호출 - proposerId: {}, proposer DB id: {}", payload.getProposerId(), proposer.getId());
        log.info("acceptProposal 호출 - recipientId: {}, recipient DB id: {}", recipient.getId(), recipient.getId());
        // 두 개의 Partnership 조회
        Partnership p1 = partnershipRepo.findByRequesterAndPartner(proposer, recipient)
                .orElseThrow(() -> new IllegalArgumentException("Partnership not found"));
        Partnership p2 = partnershipRepo.findByRequesterAndPartner(recipient, proposer)
                .orElseThrow(() -> new IllegalArgumentException("Partnership not found"));

        // 상태 변경
        p1.setStatus(PartnershipStatus.COMPLETED);
        p2.setStatus(PartnershipStatus.COMPLETED);
        partnershipRepo.save(p1);
        partnershipRepo.save(p2);

        // CouponTemplate 저장 (기존대로)
        CouponDTO proposerCoupon = payload.getProposerCoupon();
        CouponDTO recipientCoupon = payload.getRecipientCoupon();

        CouponTemplate ct1 = CouponTemplate.builder()
                .partnership(p1)
                .applicableBusinessId(proposer.getId())
                .item(proposerCoupon.getItemName())
                .discountValue(Long.valueOf(proposerCoupon.getDiscountPercent()))
                .totalQuantity(proposerCoupon.getTotalQuantity())
                .startDate(proposerCoupon.getStartDate())
                .endDate(proposerCoupon.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        CouponTemplate ct2 = CouponTemplate.builder()
                .partnership(p2)
                .applicableBusinessId(recipient.getId())
                .item(recipientCoupon.getItemName())
                .discountValue(Long.valueOf(recipientCoupon.getDiscountPercent()))
                .totalQuantity(recipientCoupon.getTotalQuantity())
                .startDate(recipientCoupon.getStartDate())
                .endDate(recipientCoupon.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        templateRepo.save(ct1);
        templateRepo.save(ct2);

        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), Map.of(
                "type", "PROPOSAL_ACCEPTED",
                "partnershipIds", List.of(p1.getId(), p2.getId()),
                "couponTemplateIds", List.of(ct1.getId(), ct2.getId()),
                "acceptedBy", recipient.getId(),
                "timestamp", System.currentTimeMillis()
        ));
    }


    // 4️⃣ 제안 거절
    @Transactional
    public void rejectProposal(Long messageId, String reason) throws JsonProcessingException {
        ChatMessage proposalMessage = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal message not found"));

        ChatRoom room = proposalMessage.getRoom();
        ProposalPayloadDTO payload = objectMapper.readValue(proposalMessage.getContent(), ProposalPayloadDTO.class);
        payload.setStatus("REJECTED");
        proposalMessage.setContent(objectMapper.writeValueAsString(payload));
        messageRepo.save(proposalMessage);


        Business proposer = userRepo.findById(payload.getProposerId())
                .orElseThrow(() -> new IllegalArgumentException("Proposer not found"));

        Business recipient = userRepo.findById(
                Objects.equals(room.getRequester().getId(), proposer.getId()) ? room.getRecipient().getId() : room.getRequester().getId()
        ).orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // 두 개의 Partnership 조회
        Partnership p1 = partnershipRepo.findByRequesterAndPartner(proposer, recipient)
                .orElseThrow(() -> new IllegalArgumentException("Partnership not found"));
        Partnership p2 = partnershipRepo.findByRequesterAndPartner(recipient, proposer)
                .orElseThrow(() -> new IllegalArgumentException("Partnership not found"));

//        // 상태 변경
//        p1.setStatus(PartnershipStatus.REJECTED);
//        p2.setStatus(PartnershipStatus.REJECTED);
//        partnershipRepo.save(p1);
//        partnershipRepo.save(p2);

        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), Map.of(
                "type", "PROPOSAL_REJECTED",
                "proposalMessageId", messageId,
                "rejectedBy", recipient.getId(),
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
            map.put("description", p.getItem());
            map.put("discountValue", p.getDiscountValue());
            map.put("totalQuantity", p.getTotalQuantity());
            map.put("startDate", p.getStartDate());
            map.put("endDate", p.getEndDate());
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
                builder.content("쿠폰 교환 제안");
                builder.payload(payload);
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
                result.put("payload", payload);
            } catch (Exception e) {
                result.put("content", "쿠폰 교환 제안 (파싱 오류)");
                result.put("payload", null);
            }
        } else {
            result.put("content", m.getContent());
        }
        log.info(result.toString());
        return result;
    }
}

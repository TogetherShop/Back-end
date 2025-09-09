package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.ChatMessage;
import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.domain.Partnership;
import com.togethershop.backend.dto.*;
import com.togethershop.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnershipService {

    private final ChatRoomRepository roomRepo;
    private final ShopUserRepository userRepo;
    private final ChatMessageRepository messageRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final PartnershipRepository partnershipRepo;
    private final BusinessRepository businessRepo;

    /**
     * 협업 요청 생성
     */
    @Transactional
    public ChatRoom createRequest(Long requesterId, Long recipientId, String message) {
        Business requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("요청자를 찾을 수 없습니다"));
        Business recipient = userRepo.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다"));

        // 요청 보내자마자 Partnership 2개 생성
        LocalDate now = LocalDate.now();

        Partnership p1 = Partnership.builder()
                .requester(requester)
                .partner(recipient)
                .status(PartnershipStatus.REQUESTED)
                .startDate(now)
                .build();

        Partnership p2 = Partnership.builder()
                .requester(recipient)
                .partner(requester)
                .status(PartnershipStatus.REQUESTED)
                .startDate(now)
                .build();

        partnershipRepo.save(p1);
        partnershipRepo.save(p2);

        // 채팅방 생성
        ChatRoom room = ChatRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .requester(requester)
                .recipient(recipient)
                .status(ChatStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .partnership(p1) // 방에는 requester 기준 하나만 연결
                .build();

        room = roomRepo.save(room);

        // 최초 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .room(room)
                .senderId(requester.getId())
                .receiverBusinessId(recipient.getId())
                .type(MessageType.PARTNERSHIP_REQUEST)
                .content(message)
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .partnership(p1)
                .build();
        messageRepo.save(chatMessage);

        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(),
                buildPartnershipMessage(chatMessage, ChatStatus.WAITING, null));

        return room;
    }


    /**
     * 요청 수락
     */
    @Transactional
    public void acceptRequest(String roomId, Long shopId) {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        if (!room.getRecipient().getId().equals(shopId)) {
            throw new AccessDeniedException("요청을 수락할 권한이 없습니다");
        }

        room.setStatus(ChatStatus.IN_NEGOTIATION);
        roomRepo.save(room);

        // 파트너십 상태 업데이트
        Partnership p1 = room.getPartnership(); // requester 기준
        Partnership p2 = partnershipRepo.findByRequesterAndPartner(p1.getPartner(), p1.getRequester())
                .orElseThrow(() -> new IllegalArgumentException("반대 파트너십을 찾을 수 없습니다"));

        p1.setStatus(PartnershipStatus.ACCEPTED);
        p2.setStatus(PartnershipStatus.ACCEPTED);

        partnershipRepo.save(p1);
        partnershipRepo.save(p2);

        // 시스템 메시지
        ChatMessage sysMessage = ChatMessage.builder()
                .room(room)
                .senderId(shopId)
                .receiverBusinessId(room.getRequester().getId())
                .type(MessageType.PARTNERSHIP_REQUEST)
                .content("요청이 수락되었습니다")
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .partnership(p1)
                .build();
        messageRepo.save(sysMessage);

        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                buildPartnershipMessage(sysMessage, ChatStatus.ACCEPTED, null));
    }


    /**
     * 요청 거절
     */
    @Transactional
    public void rejectRequest(String roomId, String username, String reason) {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));
        Business user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (!room.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedException("요청을 거절할 권한이 없습니다");
        }

        // 채팅방 상태 변경
        room.setStatus(ChatStatus.REJECTED);
        roomRepo.save(room);

        // Partnership 상태 변경
        Partnership p1 = room.getPartnership(); // requester 기준
        Partnership p2 = partnershipRepo.findByRequesterAndPartner(p1.getPartner(), p1.getRequester())
                .orElseThrow(() -> new IllegalArgumentException("반대 파트너십을 찾을 수 없습니다"));

        p1.setStatus(PartnershipStatus.REJECTED);
        p2.setStatus(PartnershipStatus.REJECTED);
        partnershipRepo.save(p1);
        partnershipRepo.save(p2);

        // 시스템 메시지
        ChatMessage sysMessage = ChatMessage.builder()
                .room(room)
                .senderId(user.getId())
                .receiverBusinessId(room.getRequester().getId())
                .type(MessageType.PARTNERSHIP_REQUEST)
                .content("요청이 거절되었습니다")
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .partnership(p1)
                .build();
        messageRepo.save(sysMessage);

        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                buildPartnershipMessage(sysMessage, ChatStatus.REJECTED, reason));
    }


    @Transactional
    public void sendTextMessage(String roomId, Long senderId, String content) {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .senderId(senderId)
                .receiverBusinessId(room.getRequester().getId().equals(senderId) ?
                        room.getRecipient().getId() : room.getRequester().getId())
                .type(MessageType.TEXT)
                .content(content)
                .deliveryStatus(MessageDeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .partnership(room.getPartnership())
                .build();
        messageRepo.save(msg);

        Map<String, Object> stompMessage = new HashMap<>();
        long timestamp = msg.getSentAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        stompMessage.put("id", msg.getId());
        stompMessage.put("type", "TEXT");
        stompMessage.put("senderId", senderId);
        stompMessage.put("senderName", getUsernameById(senderId));
        stompMessage.put("content", msg.getContent());
        stompMessage.put("createdAt", timestamp);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, stompMessage);
    }

    /**
     * 클라이언트용 메시지 DTO 변환
     */
    private Map<String, Object> buildPartnershipMessage(ChatMessage msg, ChatStatus status, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestId", msg.getRoom().getRoomId());
        payload.put("status", status.name());
        payload.put("message", msg.getContent());
        payload.put("reason", reason);

        Map<String, Object> stompMessage = new HashMap<>();
        LocalDateTime createdAt = msg.getSentAt(); // LocalDateTime
        long epochMilli = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        stompMessage.put("timestamp", epochMilli);
        stompMessage.put("type", "PARTNERSHIP_REQUEST");
        stompMessage.put("sender", msg.getSenderId() != null ? getUsernameById(msg.getSenderId()) : "SYSTEM");
        stompMessage.put("payload", payload);

        return stompMessage;
    }

    private String getUsernameById(Long id) {
        return userRepo.findById(id).map(Business::getUsername).orElse("UNKNOWN");
    }

    public List<PartnershipDTO> getAllBusinesses(Long currentUserId) {
        List<Business> businesses = businessRepo.findAll();

        return businesses.stream().map(b -> {
            boolean partnershipExists = partnershipRepo.existsPartnership(currentUserId, b.getId());
            return new PartnershipDTO(
                    b.getId(),
                    b.getBusinessName(),
                    b.getBusinessCategory(),
                    partnershipExists
            );
        }).collect(Collectors.toList());
    }

}

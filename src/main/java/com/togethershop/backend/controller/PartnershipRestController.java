package com.togethershop.backend.controller;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.ChatMessage;
import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.domain.Partnership;
import com.togethershop.backend.dto.ChatHistoryResponseDTO;
import com.togethershop.backend.dto.ChatMessageResponseDTO;
import com.togethershop.backend.dto.MessageType;
import com.togethershop.backend.repository.ChatMessageRepository;
import com.togethershop.backend.repository.ChatRoomRepository;
import com.togethershop.backend.repository.PartnershipRepository;
import com.togethershop.backend.repository.ShopUserRepository;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.PartnershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partnership")
@RequiredArgsConstructor
@Slf4j
public class PartnershipRestController {

    private final PartnershipService partnershipService;
    private final ChatRoomRepository roomRepo;
    private final ShopUserRepository userRepo;
    private final ChatMessageRepository messageRepo;
    private final PartnershipRepository partnershipRepo;

    @PostMapping("/request/{recipientId}")
    public ResponseEntity<?> requestPartnership(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recipientId,
            @RequestParam(value = "message", defaultValue = "협업을 제안합니다.") String message) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "인증이 필요합니다"));
        }

        ChatRoom room = partnershipService.createRequest(
                userDetails.getUserId(),
                recipientId,
                message
        );
        return ResponseEntity.ok(Map.of("roomId", room.getRoomId()));
    }

    @PostMapping("/accept/{roomId}")
    public ResponseEntity<?> acceptRequest(
            @PathVariable String roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        partnershipService.acceptRequest(roomId, userDetails.getUserId());
        log.info("Accepted request for user {}", userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "요청이 수락되었습니다."
        ));
    }

    @PostMapping("/reject/{roomId}")
    public ResponseEntity<?> rejectRequest(
            @PathVariable String roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) Map<String, String> request) {

        String reason = request != null ? request.getOrDefault("reason", "") : "";
        partnershipService.rejectRequest(roomId, userDetails.getUsername(), reason);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "요청이 거절되었습니다."
        ));
    }

    /**
     * Partnership 상태 조회 헬퍼
     */
    private String getPartnershipStatus(ChatRoom room) {
        Business requester = room.getRequester();
        Business recipient = room.getRecipient();
        Partnership partnership = partnershipRepo.findByRequesterAndPartner(requester, recipient)
                .orElse(null);
        return partnership != null ? partnership.getStatus().name() : "REQUESTED";
    }

    /**
     * 방 목록 조회 (status = Partnership 기준)
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> myRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "q", required = false) String query) {

        Long userId = userDetails.getUserId();
        Business user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        List<ChatRoom> rooms = roomRepo.findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(
                user.getId(), user.getId());

        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            rooms = rooms.stream()
                    .filter(r -> {
                        boolean isRequester = r.getRequester().getId().equals(user.getId());
                        Business otherUser = isRequester ? r.getRecipient() : r.getRequester();
                        return otherUser.getBusinessName().toLowerCase().contains(q)
                                || (otherUser.getBusinessCategory() != null
                                && otherUser.getBusinessCategory().toLowerCase().contains(q));
                    })
                    .toList();
        }

        List<Map<String, Object>> dto = rooms.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", r.getRoomId());

            // Partnership 상태 기준
            map.put("status", getPartnershipStatus(r));

            map.put("createdAt", r.getCreatedAt());

            boolean isRequester = r.getRequester().getId().equals(user.getId());
            Business otherUser = isRequester ? r.getRecipient() : r.getRequester();
            map.put("otherShop", otherUser.getBusinessName());
            map.put("otherUserId", otherUser.getId());
            map.put("role", isRequester ? "REQUESTER" : "RECIPIENT");
            map.put("otherUserCategory", otherUser.getBusinessCategory());
            return map;
        }).toList();

        return ResponseEntity.ok(dto);
    }

    /**
     * 채팅 기록 조회 (payload 포함, status = Partnership 기준)
     */
    @GetMapping("/rooms/{roomId}/history")
    public ResponseEntity<ChatHistoryResponseDTO> history(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        Long userId = userDetails.getUserId();
        if (!room.getRequester().getId().equals(userId) &&
                !room.getRecipient().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        String partnershipStatus = getPartnershipStatus(room);

        // 현재 접속자 기준
        boolean isRequester = room.getRequester().getId().equals(userId);
        Business me = isRequester ? room.getRequester() : room.getRecipient();
        Business otherUser = isRequester ? room.getRecipient() : room.getRequester();

        Page<ChatMessage> msgs = messageRepo.findByRoomRoomIdOrderBySentAtAsc(
                roomId, PageRequest.of(page, size));

        List<ChatMessageResponseDTO> result = msgs.getContent().stream().map(m -> {
            String senderName = (m.getSenderId() != null)
                    ? userRepo.findById(m.getSenderId())
                    .map(Business::getUsername)
                    .orElse("UNKNOWN")
                    : "SYSTEM";

            ChatMessageResponseDTO.ChatMessageResponseDTOBuilder builder = ChatMessageResponseDTO.builder()
                    .id(m.getId())
                    .type(m.getType().name())
                    .senderId(m.getSenderId())
                    .senderName(senderName)
                    .content(m.getContent())
                    .createdAt(m.getSentAt());

            if (m.getType() == MessageType.PARTNERSHIP_REQUEST) {
                // payload에서도 현재 접속자 기준으로 requester/recipient
                builder.payload(ChatMessageResponseDTO.Payload.builder()
                        .requestId(m.getId())
                        .status(partnershipStatus)
                        .message(m.getContent())
                        .requesterId(room.getRequester().getId())
                        .recipientId(room.getRecipient().getId())
                        .build());
            }

            return builder.build();
        }).toList();

        ChatHistoryResponseDTO response = ChatHistoryResponseDTO.builder()
                .messages(result)
                .totalElements(msgs.getTotalElements())
                .totalPages(msgs.getTotalPages())
                .currentPage(page)
                .roomInfo(ChatHistoryResponseDTO.RoomInfo.builder()
                        .roomId(room.getRoomId())
                        .status(partnershipStatus)
                        .currentUserId(userId)
                        .requesterId(room.getRequester().getId())
                        .recipientId(room.getRecipient().getId())
                        .me(ChatHistoryResponseDTO.UserInfo.builder()
                                .id(me.getId())
                                .username(me.getUsername())
                                .shopName(me.getBusinessName())
                                .build())
                        .otherUser(ChatHistoryResponseDTO.UserInfo.builder()
                                .id(otherUser.getId())
                                .username(otherUser.getUsername())
                                .shopName(otherUser.getBusinessName())
                                .build())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }


    /**
     * 단일 방 조회 (status = Partnership 기준)
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getRoomInfo(
            @PathVariable String roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다"));

        Long userId = userDetails.getUserId();
        if (!room.getRequester().getId().equals(userId) &&
                !room.getRecipient().getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "접근 권한이 없습니다"));
        }
        String partnershipStatus = getPartnershipStatus(room);
        boolean isRequester = room.getRequester().getId().equals(userId);
        Business me = isRequester ? room.getRequester() : room.getRecipient();
        Business otherUser = isRequester ? room.getRecipient() : room.getRequester();

        return ResponseEntity.ok(Map.of(
                "roomId", room.getRoomId(),
                "status", getPartnershipStatus(room),
                "createdAt", room.getCreatedAt(),
                "role", isRequester ? "REQUESTER" : "RECIPIENT",
                "requesterId", room.getRequester().getId(),
                "recipientId", room.getRecipient().getId(),
                "partnershipStatus", getPartnershipStatus(room),
                "me", Map.of(
                        "id", me.getId(),
                        "username", me.getUsername(),
                        "shopName", me.getBusinessName()
                ),
                "otherUser", Map.of(
                        "id", otherUser.getId(),
                        "username", otherUser.getUsername(),
                        "shopName", otherUser.getBusinessName()
                )
        ));

    }

    /**
     * Instant → LocalDateTime 변환 헬퍼
     */
    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}

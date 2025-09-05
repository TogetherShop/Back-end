package com.togethershop.backend.controller;

import com.togethershop.backend.domain.ChatMessage;
import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.domain.ShopUser;
import com.togethershop.backend.dto.ChatHistoryResponseDTO;
import com.togethershop.backend.dto.ChatMessageResponseDTO;
import com.togethershop.backend.dto.MessageType;
import com.togethershop.backend.repository.ChatMessageRepository;
import com.togethershop.backend.repository.ChatRoomRepository;
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
     * ✅ 누락됐던 방 목록 API 복구
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> myRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        ShopUser user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        List<ChatRoom> rooms = roomRepo.findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(
                user.getId(), user.getId());

        // (빠른 적용 위해 Map 유지. 필요시 DTO로 빼드릴 수 있어요)
        List<Map<String, Object>> dto = rooms.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", r.getRoomId());
            map.put("status", r.getStatus().name()); // enum → String
            map.put("createdAt", r.getCreatedAt());

            boolean isRequester = r.getRequester().getId().equals(user.getId());
            ShopUser otherUser = isRequester ? r.getRecipient() : r.getRequester();
            map.put("otherShop", otherUser.getShopName());
            map.put("otherUserId", otherUser.getId());
            map.put("role", isRequester ? "REQUESTER" : "RECIPIENT");
            return map;
        }).toList();

        return ResponseEntity.ok(dto);
    }

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

        Page<ChatMessage> msgs = messageRepo.findByRoomRoomIdOrderByCreatedAtAsc(
                roomId, PageRequest.of(page, size));

        List<ChatMessageResponseDTO> result = msgs.getContent().stream().map(m -> {
            String senderName = (m.getSenderId() != null)
                    ? userRepo.findById(m.getSenderId())
                    .map(ShopUser::getUsername)
                    .orElse("UNKNOWN")
                    : "SYSTEM";

            ChatMessageResponseDTO.ChatMessageResponseDTOBuilder builder = ChatMessageResponseDTO.builder()
                    .id(m.getId())
                    .type(m.getType().name()) // enum → String
                    .senderId(m.getSenderId())
                    .senderName(senderName)
                    .content(m.getContent())
                    .createdAt(m.getCreatedAt()); // Instant → LocalDateTime

            // ✅ PARTNERSHIP_REQUEST일 때 payload 채워서 프론트 조건부 렌더링 지원
            if (m.getType() == MessageType.PARTNERSHIP_REQUEST) {
                builder.payload(ChatMessageResponseDTO.Payload.builder()
                        .requestId(m.getId())
                        .status(room.getStatus().name()) // enum → String
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
                        .status(room.getStatus().name()) // enum → String
                        .currentUserId(userId)
                        .requesterId(room.getRequester().getId())
                        .recipientId(room.getRecipient().getId())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

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

        boolean isRequester = room.getRequester().getId().equals(userId);
        ShopUser otherUser = isRequester ? room.getRecipient() : room.getRequester();

        return ResponseEntity.ok(Map.of(
                "roomId", room.getRoomId(),
                "status", room.getStatus().name(),
                "createdAt", room.getCreatedAt(),
                "role", isRequester ? "REQUESTER" : "RECIPIENT",
                "requesterId", room.getRequester().getId(),
                "recipientId", room.getRecipient().getId(),
                "otherUser", Map.of(
                        "id", otherUser.getId(),
                        "username", otherUser.getUsername(),
                        "shopName", otherUser.getShopName()
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

package com.togethershop.backend.controller;

import com.togethershop.backend.dto.ProposalPayloadDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.ChatService;
import com.togethershop.backend.service.PartnershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompChatController {

    private final ChatService chatService;
    private final PartnershipService partnershipService;
    private final SimpMessagingTemplate messagingTemplate;

    // 1️⃣ 텍스트 메시지 전송
    @MessageMapping("/chat.send")
    public void sendText(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");
        String content = (String) payload.get("content");

        chatService.sendTextMessage(roomId, userDetails.getUserId(), content);
    }

    // 2️⃣ 쿠폰 제안
    @MessageMapping("/chat.propose")
    public void propose(@Payload ProposalPayloadDTO dto, Principal principal) throws Exception {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);

        if (dto.getProposerCoupon() == null) {
            throw new IllegalArgumentException("proposerCoupon 정보가 필요합니다");
        }


        // ✅ CouponDTO 그대로 전달
        chatService.proposeCoupon(dto.getRoomId(), userDetails.getUserId(), dto);
    }

    // 3️⃣ 제안 수락
    @MessageMapping("/chat.proposal.accept")
    public void acceptProposal(@Payload Map<String, Object> payload, Principal principal) throws Exception {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        Long proposalId = getLongFromMap(payload, "proposalId");

        if (proposalId == null) {
            log.warn("proposalId가 없습니다.");
            return;
        }

        chatService.acceptProposal(proposalId);
    }

    // 4️⃣ 제안 거절
    @MessageMapping("/chat.proposal.reject")
    public void rejectProposal(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        Long proposalId = getLongFromMap(payload, "proposalId");
        String reason = (String) payload.get("reason");

        if (proposalId == null) {
            log.warn("proposalId가 없습니다.");
            return;
        }

        chatService.rejectProposal(proposalId, reason);
    }

    // 5️⃣ 제안 상태 조회
    @MessageMapping("/chat.proposal.status")
    public void getProposalStatus(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");

        try {
            Map<String, Object> status = chatService.propose(roomId);
            messagingTemplate.convertAndSendToUser(userDetails.getUsername(), "/queue/proposal-status", Map.of(
                    "type", "PROPOSAL_STATUS",
                    "roomId", roomId,
                    "status", status,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("제안 상태 조회 실패 - roomId: {}, user: {}", roomId, userDetails.getUsername(), e);
        }
    }

    // 협업 요청 수락
    @MessageMapping("/chat.request.accept")
    public void acceptRequest(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");
        partnershipService.acceptRequest(roomId, userDetails.getUserId());
    }

    // 협업 요청 거절
    @MessageMapping("/chat.request.reject")
    public void rejectRequest(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");
        String reason = (String) payload.get("reason");
        partnershipService.rejectRequest(roomId, userDetails.getUsername(), reason);
    }

    private CustomUserDetails getUserFromPrincipal(Principal principal) {
        if (principal instanceof Authentication auth) {
            Object principalObj = auth.getPrincipal();
            if (principalObj instanceof CustomUserDetails userDetails) {
                return userDetails;
            }
        }
        throw new IllegalStateException("사용자 정보가 없습니다");
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number num) return num.longValue();
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}

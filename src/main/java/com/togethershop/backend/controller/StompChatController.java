package com.togethershop.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.togethershop.backend.dto.ChatMessageDTO;
import com.togethershop.backend.dto.CouponDTO;
import com.togethershop.backend.dto.ProposalPayloadDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.ChatService;
import com.togethershop.backend.service.PartnershipService;
import com.togethershop.backend.service.RedisChatPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompChatController {

    private final ChatService chatService;
    private final PartnershipService partnershipService;
    private final RedisChatPublisher publisher;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendText(@Payload ChatMessageDTO dto, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        // PartnershipService로 보내기
        partnershipService.sendTextMessage(dto.getRoomId(), userDetails.getUserId(), dto.getContent());
    }

    // 쿠폰 제안
    @MessageMapping("/chat.propose")
    public void propose(@Payload ProposalPayloadDTO dto, Principal principal) throws JsonProcessingException {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = dto.getRoomId();
        String username = userDetails.getUsername();

        if (dto.getPayload() == null ||
                dto.getPayload().getProposerCoupon() == null ||
                dto.getPayload().getRecipientCoupon() == null) {
            throw new IllegalArgumentException("proposerCoupon과 recipientCoupon 정보가 필요합니다");
        }

        CouponDTO proposer = dto.getPayload().getProposerCoupon();
        CouponDTO recipient = dto.getPayload().getRecipientCoupon();

        // 날짜 파싱
        LocalDate myStart = LocalDate.parse(proposer.getStartDate());
        LocalDate myEnd = LocalDate.parse(proposer.getEndDate());
        LocalDate theirStart = LocalDate.parse(recipient.getStartDate());
        LocalDate theirEnd = LocalDate.parse(recipient.getEndDate());

        log.info("Processing coupon proposal - My Item: {}, Partner Item: {}", proposer.getItemName(), recipient.getItemName());
        log.info("My coupon: {}%/{} qty/{} to {}", proposer.getDiscountPercent(), proposer.getTotalQuantity(), myStart, myEnd);
        log.info("Their coupon: {}%/{} qty/{} to {}", recipient.getDiscountPercent(), recipient.getTotalQuantity(), theirStart, theirEnd);

        chatService.proposeCoupon(
                roomId,
                username,
                proposer,
                recipient
        );
    }

    // 제안 수락
    @MessageMapping("/chat.proposal.accept")
    public void acceptProposal(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");
        Long accepterId = userDetails.getUserId();

        // 입력 검증
        if (roomId == null || roomId.trim().isEmpty()) {
            log.warn("잘못된 제안 수락 요청 - accepterId={}, roomId={}", accepterId, roomId);
            return;
        }

        log.info("제안 수락 요청 - 사용자: {}, 방: {}", accepterId, roomId);

        try {
            chatService.acceptProposal(roomId, accepterId);

            // 성공 메시지를 개별 사용자에게 전송

        } catch (IllegalArgumentException e) {
            log.warn("제안 수락 실패 - 사용자: {}, 방: {}, 이유: {}", accepterId, roomId, e.getMessage());


        } catch (JsonProcessingException e) {
            log.error("제안 데이터 파싱 오류 - 사용자: {}, 방: {}", accepterId, roomId, e);

        } catch (Exception e) {
            log.error("제안 수락 중 예상치 못한 오류 - 사용자: {}, 방: {}", accepterId, roomId, e);
        }
    }

    // 제안 거절
    @MessageMapping("/chat.proposal.reject")
    public void rejectProposal(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");
        Long rejecterId = userDetails.getUserId();
        String reason = (String) payload.get("reason");

        // 입력 검증
        if (roomId == null || roomId.trim().isEmpty()) {
            log.warn("잘못된 제안 거절 요청 - rejecterId={}, roomId={}", rejecterId, roomId);
            return;
        }

        log.info("제안 거절 요청 - 사용자: {}, 방: {}, 이유: {}", rejecterId, roomId, reason);

        try {
            chatService.rejectProposal(roomId, rejecterId, reason);

        } catch (IllegalArgumentException e) {
            log.warn("제안 거절 실패 - 사용자: {}, 방: {}, 이유: {}", rejecterId, roomId, e.getMessage());

        } catch (Exception e) {
            log.error("제안 거절 중 예상치 못한 오류 - 사용자: {}, 방: {}", rejecterId, roomId, e);
        }
    }

    // 협업 요청 수락
    @MessageMapping("/chat.request.accept")
    public void acceptRequest(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");
        Long shopId = userDetails.getUserId();
        log.info("Accepted request for user {}", shopId);
        partnershipService.acceptRequest(roomId, shopId);
    }

    // 협업 요청 거절
    @MessageMapping("/chat.request.reject")
    public void rejectRequest(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String username = userDetails.getUsername();
        String roomId = (String) payload.get("roomId");
        String reason = (String) payload.get("reason");
        partnershipService.rejectRequest(roomId, username, reason);
    }

    /**
     * Map에서 Integer 값을 안전하게 추출하는 헬퍼 메소드
     */
    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("정수 파싱 실패 - key: {}, value: {}", key, value);
                return null;
            }
        }
        log.warn("정수 변환 불가 - key: {}, value: {}, type: {}", key, value, value.getClass());
        return null;
    }

    /**
     * Principal에서 CustomUserDetails 추출 (STOMP/WebSocket용 안전 캐스팅)
     */
    private CustomUserDetails getUserFromPrincipal(Principal principal) {
        if (principal instanceof Authentication auth) {
            Object principalObj = auth.getPrincipal();
            if (principalObj instanceof CustomUserDetails userDetails) {
                return userDetails;
            }
        }
        throw new IllegalStateException("사용자 정보가 없습니다");
    }

    // Controller에 추가
    @MessageMapping("/chat.proposal.status")
    public void getProposalStatus(@Payload Map<String, Object> payload, Principal principal) {
        CustomUserDetails userDetails = getUserFromPrincipal(principal);
        String roomId = (String) payload.get("roomId");

        if (roomId == null || roomId.trim().isEmpty()) {
            log.info("방 ID가 필요합니다.");
            return;
        }

        try {
            Map<String, Object> status = chatService.getProposalStatus(roomId);

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
}
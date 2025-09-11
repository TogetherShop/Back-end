package com.togethershop.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ChatMessageResponseDTO {
    private Long id;
    private String type;
    private Long senderId;
    private String senderName;
    private String content;
    private Instant createdAt;
    private Payload payload;

    @Getter
    @Builder
    public static class Payload {
        private Long requestId;
        private String status;
        private String message;
        private Long requesterId;
        private Long recipientId;
        private CouponProposalDTO proposal;
    }
}


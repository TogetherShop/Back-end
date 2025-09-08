package com.togethershop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private String roomId;
    private Long senderId;
    private String senderName;
    private String businessName;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private boolean isTemp;
    private ProposalPayloadDTO payload;
}

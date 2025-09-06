package com.togethershop.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatHistoryResponseDTO {
    private List<ChatMessageResponseDTO> messages;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private RoomInfo roomInfo;

    @Getter
    @Builder
    public static class RoomInfo {
        private String roomId;
        private String status;
        private Long currentUserId;
        private Long requesterId;
        private Long recipientId;
    }
}


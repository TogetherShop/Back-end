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
        private UserInfo me;          // 현재 접속자 정보
        private UserInfo otherUser;
    }


    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String shopName;
    }
}


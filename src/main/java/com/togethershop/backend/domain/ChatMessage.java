package com.togethershop.backend.domain;

import com.togethershop.backend.dto.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ChatMessage.java
@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    private Long senderId; // ShopUser.id

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private MessageType type;

    @Column(columnDefinition = "TEXT")
    private String content; // 텍스트 or JSON(제안 데이터 요약)

    private LocalDateTime createdAt;
}


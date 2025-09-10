package com.togethershop.backend.domain;

import com.togethershop.backend.dto.ChatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ChatRoom.java
@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomId; // 예: UUID

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatStatus status = ChatStatus.WAITING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "partnership_id")
    private Long partnershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", insertable = false, updatable = false)
    private Business requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    private Business recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partnership_id", insertable = false, updatable = false)
    private Partnership partnership;
}


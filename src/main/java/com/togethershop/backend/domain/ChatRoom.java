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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Business requester; // 요청자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Business recipient; // 수신자

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatStatus status = ChatStatus.WAITING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "partnership_id")
    private Partnership partnership;
}


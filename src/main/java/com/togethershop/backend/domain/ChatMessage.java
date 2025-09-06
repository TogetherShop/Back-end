package com.togethershop.backend.domain;

import com.togethershop.backend.dto.MessageDeliveryStatus;
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

    @Column(name = "sender_business_id", nullable = false)
    private Long senderId; // ShopUser.id

    @Column(name = "receiver_business_id", nullable = false)
    private Long receiverBusinessId;


    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private MessageType type;
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "proposal_data", columnDefinition = "JSON")
    private String proposalData;
    @Column(columnDefinition = "TEXT")
    private String content; // 텍스트 or JSON(제안 데이터 요약)

    @Column(name = "sent_at", insertable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // 메시지 전송 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MessageDeliveryStatus deliveryStatus = MessageDeliveryStatus.SENT;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partnership_id", nullable = false) // DB 컬럼과 매핑
    private Partnership partnership;
}


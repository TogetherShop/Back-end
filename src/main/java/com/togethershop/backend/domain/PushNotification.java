package com.togethershop.backend.domain;

import com.togethershop.backend.dto.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "data", columnDefinition = "json")
    private String data;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private NotificationStatus status = NotificationStatus.SENT;

    public enum NotificationType {
        LOCATION_BASED, COUPON, PARTNERSHIP, MARKETING, SYSTEM
    }

}

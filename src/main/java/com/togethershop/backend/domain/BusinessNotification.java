package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "business_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessNotificationId;

    private Long businessId;
    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(columnDefinition = "json")
    private String data; // JSON String

    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime clickedAt;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
}

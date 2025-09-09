package com.togethershop.backend.dto;
import java.time.LocalDateTime;

import com.togethershop.backend.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponseDTO {
    private String message;
    private NotificationType notificationType;
    private LocalDateTime sentAt;
}

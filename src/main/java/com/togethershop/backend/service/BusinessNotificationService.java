package com.togethershop.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import com.togethershop.backend.domain.BusinessNotification;
import com.togethershop.backend.dto.NotificationResponseDTO;
import com.togethershop.backend.repository.BusinessNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessNotificationService {

    private final BusinessNotificationRepository notificationRepo;

    public BusinessNotificationService(BusinessNotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnreadNotificationsByBusiness(Long businessId) {
        List<BusinessNotification> notifications = notificationRepo.findUnreadNotifications(businessId);

        return notifications.stream()
                .map(n -> NotificationResponseDTO.builder()
                        .message(n.getMessage())
                        .notificationType(n.getNotificationType())
                        .sentAt(n.getSentAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean markNotificationAsRead(Long notificationId) {
        int updated = notificationRepo.markAsRead(notificationId);
        return updated > 0;
    }
}

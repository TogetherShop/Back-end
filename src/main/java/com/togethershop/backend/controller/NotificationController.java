package com.togethershop.backend.controller;


import com.togethershop.backend.dto.NotificationResponseDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.BusinessNotificationService;
import com.togethershop.backend.service.CustomerNotificationService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final CustomerNotificationService customerNotificationService;
    private final BusinessNotificationService businessNotificationService;


    @GetMapping("/customer")
    public ResponseEntity<List<NotificationResponseDTO>> getCustomerNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        List<NotificationResponseDTO> notificationDto = customerNotificationService.getReadNotificationsByCustomer(user.getUserId());
        return ResponseEntity.ok(notificationDto);
    }

    @GetMapping("/business")
    public ResponseEntity<List<NotificationResponseDTO>> getBusinessNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        List<NotificationResponseDTO> notificationDto = businessNotificationService.getUnreadNotificationsByBusiness(user.getUserId());
        return ResponseEntity.ok(notificationDto);
    }
}

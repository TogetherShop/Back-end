package com.togethershop.backend.service;

import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.domain.CustomerNotification;
import com.togethershop.backend.domain.NotificationStatus;
import com.togethershop.backend.domain.NotificationType;
import com.togethershop.backend.dto.FcmSendDTO;
import com.togethershop.backend.repository.CustomerNotificationRepository;
import com.togethershop.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerNotificationService {

    private final CustomerNotificationRepository notificationRepo;
    private final CustomerRepository customerRepo;
    private final FcmService fcmService;

    @Transactional
    public void sendCouponCreatedNotification(Long customerId, String couponName) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        String title = "쿠폰이 생성되었습니다!";
        String message = couponName + " 쿠폰이 발급되었어요.";

        CustomerNotification notification = new CustomerNotification();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(NotificationType.COUPON);
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.SENT);

        notificationRepo.save(notification);

        // FCM 토큰이 존재하면 FCM 전송 시도
        String fcmToken = customer.getCustomerFcmToken();
        if (fcmToken != null && !fcmToken.isEmpty()) {
            FcmSendDTO dto = FcmSendDTO.builder()
                    .token(fcmToken)
                    .title(title)
                    .body(message)
                    .build();

            boolean sent = fcmService.sendNotification(dto);

            if (sent) {
                notification.setStatus(NotificationStatus.DELIVERED);
            } else {
                notification.setStatus(NotificationStatus.FAILED);
            }
            notificationRepo.save(notification);
        }
    }

}


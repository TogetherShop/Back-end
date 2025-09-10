package com.togethershop.backend.util;

import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.domain.CouponTemplate;
import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.dto.CouponStatus;
import com.togethershop.backend.dto.FcmSendDTO;
import com.togethershop.backend.repository.CouponRepository;
import com.togethershop.backend.repository.CouponTemplateRepository;
import com.togethershop.backend.repository.CustomerRepository;
import com.togethershop.backend.service.CustomerNotificationService;
import com.togethershop.backend.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor

public class CouponExpiryScheduler {
    private final CouponRepository couponRepository;
    private final CustomerNotificationService customerNotificationService;
    private final CouponTemplateRepository couponTemplateRepository;
    private final FcmService fcmService;
    private final CustomerRepository customerRepository;


    // 매일 12시 0분 0초에 실행 (cron 표현식)
    @Scheduled(cron = "0 0 12 * * *")
    public void checkCouponExpiry() {
        LocalDate targetDate = LocalDate.now().plusDays(7);

        // 만료일이 targetDate인 쿠폰 조회 (예: ISSUED 상태인 것만)
        List<Coupon> expiringCoupons = couponRepository.findByExpireDateAndStatus(targetDate, CouponStatus.ISSUED);

        for (Coupon coupon : expiringCoupons) {
            Long customerId = coupon.getCustomerId();

            // 고객 정보와 쿠폰 이름 등 필요한 정보 조회
            Long templateId = coupon.getTemplateId(); // 필요 시 getter 추가
            CouponTemplate couponTemplate = couponTemplateRepository.findById(templateId).orElse(null);
            String couponName= couponTemplate.getPartnership().getRequester().getBusinessName();

            // 알림 메시지 생성
            String title = "쿠폰 만료 임박 안내";
            String body = couponName + " 쿠폰이 7일 후 만료됩니다.";

            // FCM 전송 DTO 생성
            String fcmToken = getFcmTokenForCustomer(customerId);
            if (fcmToken != null && !fcmToken.isEmpty()) {
                FcmSendDTO dto = FcmSendDTO.builder()
                        .token(fcmToken)
                        .title(title)
                        .body(body)
                        .build();

                boolean sent = fcmService.sendNotification(dto);

                // 알림 DB 저장 등 후처리 가능
                customerNotificationService.saveNotification(customerId, title, body, sent);
            }
        }
    }

    private String getFcmTokenForCustomer(Long customerId) {
        // 고객의 FCM 토큰 조회 로직 (ex: customerRepo에서 조회)
        Customer customer = customerRepository.findById(customerId).orElse(null);
        return customer.getCustomerFcmToken();
    }
}

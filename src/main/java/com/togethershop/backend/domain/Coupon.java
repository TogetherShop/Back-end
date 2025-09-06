package com.togethershop.backend.domain;

import com.togethershop.backend.dto.CouponStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;  // 쿠폰 템플릿 ID

    @Column(name = "customer_id", nullable = false)
    private Long customerId;  // 쿠폰 소유 고객 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Businesses businesses;  // 쿠폰 발급 사업자

    @Column(name = "coupon_code", unique = true)
    private String couponCode;

    @Column(name = "jti_token", unique = true, nullable = false, length = 500)
    private String jtiToken;  // JWT JTI 토큰 (보안용)

    @Column(name = "qr_code_data", columnDefinition = "TEXT")
    private String qrCodeData;  // QR 코드 데이터

    @Column(name = "pin_code", length = 10)
    private String pinCode;  // 쿠폰 PIN 코드

    @Column(name = "issue_date", columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime issueDate;  // 쿠폰 발급 일시

    @Column(name = "expire_date", nullable = false)
    private LocalDateTime expireDate;  // 쿠폰 만료 일시

    @Column(name = "used_date")
    private LocalDateTime usedDate;  // 쿠폰 사용 일시

    @Column(name = "used_business_id")
    private Long usedBusinessId;  // 쿠폰 사용 사업자 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "enum('ISSUED','USED','EXPIRED','CANCELLED') default 'ISSUED'")
    private CouponStatus status;


}
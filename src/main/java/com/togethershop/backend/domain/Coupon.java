package com.togethershop.backend.domain;

import com.togethershop.backend.dto.CouponStatus;
import com.togethershop.backend.dto.IssueChannel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Coupon.java (발급된 쿠폰 레코드)
@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "customer_id")
    private Long customerId;  // 쿠폰 소유 고객


    @Column(name = "coupon_code", nullable = false, length = 50)
    private String couponCode;
    @Column(name = "jti_token", nullable = false, length = 500)
    private String jtiToken;

    @Column(name = "qr_code_data", columnDefinition = "TEXT")
    private String qrCodeData;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    private Long discountPercent;
    private Long totalQuantity;
    @Column(name = "issue_date")
    private LocalDate issueDate;
    @Column(name = "expire_date", nullable = false)
    private LocalDate expireDate;
    @Column(name = "used_date")
    private LocalDateTime usedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CouponStatus status = CouponStatus.ISSUED;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_channel", length = 20)
    private IssueChannel issueChannel = IssueChannel.ONLINE;
    private String itemName;
    private LocalDateTime issuedAt;
    private Long roomId; // 어떤 협의에서 만들어졌는지
    @Column(name = "business_id", nullable = false)
    private Long businessId;  // 누가 사용할 쿠폰인지
}


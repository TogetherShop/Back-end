package com.togethershop.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponseDTO {
    private Long couponId;
    private Long templateId;
    private String couponCode;
    private String qrCodeData;
    private String pinCode;
    private LocalDateTime issueDate;
    private LocalDateTime expireDate;
    private LocalDateTime usedDate;
    private String status; // ISSUED, USED, EXPIRED, CANCELLED
    private String description;       // coupon_template.description

    private String businessName;      // business.business_name
    private String businessCategory;  // business.business_category
}
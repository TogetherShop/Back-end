package com.togethershop.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiringCouponDTO {
    private Long couponId;
    private String couponCode;
    private LocalDateTime expireDate;
    private Long templateId;
    private Long discountValue;       // 추가: 할인 금액 또는 값
    private String businessName;
    private String businessCategory;
    private Integer daysLeft;         // 만료까지 남은 일수
}

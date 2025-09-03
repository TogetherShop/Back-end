package com.togethershop.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class CouponIssueRequestDTO {
    private Long storeId;
    private String couponCode;
    private LocalDateTime expiredAt;
    private BigDecimal minimumOrderAmount;
}
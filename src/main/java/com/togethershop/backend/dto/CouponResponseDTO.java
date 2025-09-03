package com.togethershop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CouponResponseDTO {
    private String couponCode;
    private Long storeId;
    private String storeName;
    private LocalDateTime issuedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime usedAt;
    private String status;
    private BigDecimal minimumOrderAmount;
}

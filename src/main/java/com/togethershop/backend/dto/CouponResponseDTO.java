package com.togethershop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CouponResponseDTO {
    private String couponCode;
    private Long business_id;
    private String storeName;
    private LocalDateTime issuedDate;
    private LocalDateTime expiredDate;
    private LocalDateTime usedDate;
    private String status;
}

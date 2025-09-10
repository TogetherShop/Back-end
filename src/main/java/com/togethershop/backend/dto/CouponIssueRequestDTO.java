package com.togethershop.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponIssueRequestDTO {
    private Long businessId;
    private String couponCode;
    private LocalDateTime expiredDate;
}
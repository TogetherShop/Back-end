package com.togethershop.backend.dto;

import lombok.Builder;
import lombok.Getter;

// 쿠폰 제안 DTO
@Getter
@Builder
public class CouponProposalDTO {
    private String myItem;
    private String partnerItem;
    private int myDiscountRate;
    private int partnerDiscountRate;
    private int quantity;
    private int validDays;
}

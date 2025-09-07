package com.togethershop.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCouponsDTO {
    private BusinessDTO partnerBusiness;
    private List<CouponTemplateDTO> coupons;
}
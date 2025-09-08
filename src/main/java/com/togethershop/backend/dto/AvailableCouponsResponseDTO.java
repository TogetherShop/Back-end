package com.togethershop.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableCouponsResponseDTO {
    private List<BusinessDTO> recentBusinesses;      // 최근 결제한 매장 정보 리스트
    private List<CouponTemplateDTO> availableCoupons; // 해당 매장 제휴 쿠폰 리스트
}


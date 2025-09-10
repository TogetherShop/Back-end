package com.togethershop.backend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCouponListResponseDTO {
    private boolean success;
    private List<BusinessCouponDTO> myCoupons;
    private List<BusinessCouponDTO> receivedCoupons;
    private String message;
}
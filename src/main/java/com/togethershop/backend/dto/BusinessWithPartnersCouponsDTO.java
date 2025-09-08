package com.togethershop.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessWithPartnersCouponsDTO {
    private BusinessDTO business;
    private List<PartnerCouponsDTO> couponsByPartners;
}
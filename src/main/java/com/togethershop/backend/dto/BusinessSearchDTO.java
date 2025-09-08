package com.togethershop.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessSearchDTO {
    private Long businessId;
    private String businessName;
    private String businessCategory;
    private String address;
    private Number averageRating;
    private String addressType;  // "오프라인" 또는 "온라인" (주소에 따라 구분)
}
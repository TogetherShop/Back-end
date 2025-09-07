package com.togethershop.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedBusinessDTO {
    private Long businessId;
    private String businessName;
    private String businessCategory;
    private Double associationRate;  // 연관 방문률 (0~1)
}

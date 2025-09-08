package com.togethershop.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedBusinessDTO {
    private Long id;
    private String businessName;
    private String description;
    private String address;
    private Long visitCount;          // 방문인원 수(결제 횟수)
    private Number averageRating;
}

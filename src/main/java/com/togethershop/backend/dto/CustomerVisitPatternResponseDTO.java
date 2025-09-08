package com.togethershop.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerVisitPatternResponseDTO {
    private RelatedBusinessDTO recentBusiness;          // 최근 방문 매장 정보
    private List<RelatedBusinessDTO> relatedBusiness;     // 연관 매장 리스트
}
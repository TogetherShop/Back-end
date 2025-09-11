package com.togethershop.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHomeResponseDTO {
    private String businessName;
    private Integer togetherScore;
    private List<PartnerDTO> partners;
}

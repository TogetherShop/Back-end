package com.togethershop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartnershipDTO {
    private Long id;
    private String name;
    private String category;
    private boolean partnershipExists; // 이미

}

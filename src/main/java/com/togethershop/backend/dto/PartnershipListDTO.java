package com.togethershop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnershipListDTO {
    private Long businessId;
    private String businessName;
    private String businessCategory;
    private String businessType;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double togetherIndex;
    private String profileImageUrl;
    private String description;
    private String collaborationCategory;
    private String mainCustomer;
}

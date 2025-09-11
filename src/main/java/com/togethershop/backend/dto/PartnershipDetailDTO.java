package com.togethershop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnershipDetailDTO {
    private Long businessId;
    private String businessName;
    private String businessCategory;
    private String address;
    private Double latitude;
    private Double longitude;
    private String businessHours;
    private Double togetherIndex;
    private String profileImageUrl;
    private String description;
    private String collaborationCategory;
    private String phoneNumber;
    private String businessType;
}

package com.togethershop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponTemplateDTO {
    private Long templateId;
    private BigDecimal discountValue;
    private Integer totalQuantity;
    private Integer currentQuantity;
    private Integer maxUsePerCustomer;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long roomId;
    private Long partnerBusinessId;
    private Long businessId;
    private String businessName;          // 추가
    private String businessCategory;      // 추가
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String termsAndConditions;
    private Boolean acceptedByRequester;
    private Boolean acceptedByRecipient;
}
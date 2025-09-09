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
    private Long discountValue;
    private Integer totalQuantity;
    private Integer currentQuantity;
    private Integer maxUsePerCustomer;
    private LocalDateTime createdAt;
    private Long roomId;
    private Long applicableBusinessId;
    private String businessName;          // 추가
    private String businessCategory;      // 추가
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Boolean acceptedByRequester;
    private Boolean acceptedByRecipient;
}
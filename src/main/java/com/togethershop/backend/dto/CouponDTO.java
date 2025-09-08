package com.togethershop.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponDTO {
    private String itemName;
    private Integer discountValue;
    private Integer totalQuantity;
    private LocalDate startDate;
    private LocalDate endDate;   // discount_type
}

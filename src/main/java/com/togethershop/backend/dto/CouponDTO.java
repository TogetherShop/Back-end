package com.togethershop.backend.dto;

import lombok.Data;

@Data
public class CouponDTO {
    private String itemName;
    private Integer discountPercent;
    private Integer totalQuantity;
    private String startDate;
    private String endDate;
}

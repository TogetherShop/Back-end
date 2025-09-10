package com.togethershop.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCouponDTO {
    private Long id;
    private Long templateId;
    private String title;
    private String description;
    private Integer maxParticipants;
    private Double progress;
    private Integer remainingDays;
    private String status;
    private String businessName;
    private Long discountValue;
    private String timeAgo;
    private Boolean chatActive;
    private Long partnerId;
    private String expiredText;
    private String owner;
    private Integer currentQuantity;
    private Integer totalQuantity;
    private String termsAndConditions;
    private Boolean acceptedByRequester;
    private Boolean acceptedByRecipient;
    private Long roomId;
}
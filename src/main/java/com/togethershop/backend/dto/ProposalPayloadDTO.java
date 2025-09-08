package com.togethershop.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalPayloadDTO {
    private String roomId;
    private Long proposerId;
    private CouponDTO proposerCoupon;
    private CouponDTO recipientCoupon;
    private String status;

}

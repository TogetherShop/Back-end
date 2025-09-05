package com.togethershop.backend.dto;

import lombok.Data;

@Data
public class ProposalPayloadDTO {
    private String roomId;
    private CouponWrapper payload;

    @Data
    public static class CouponWrapper {
        private CouponDTO proposerCoupon;
        private CouponDTO recipientCoupon;
    }
}

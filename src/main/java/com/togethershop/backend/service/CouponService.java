package com.togethershop.backend.service;

import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.dto.CouponIssueRequestDTO;
import com.togethershop.backend.dto.CouponResponseDTO;

public interface CouponService {
    CouponResponseDTO issueCoupon(CouponIssueRequestDTO dto);
    CouponResponseDTO readCoupon(String couponCode);
    byte[] generateCouponQrCode(String couponCode) throws Exception;
    CouponResponseDTO useCoupon(String couponCode, String jti);
    CouponResponseDTO cancelCouponUse(String couponCode);
}

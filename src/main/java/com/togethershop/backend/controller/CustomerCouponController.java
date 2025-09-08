package com.togethershop.backend.controller;

import com.togethershop.backend.dto.*;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.CustomerCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customer/coupons")
@RequiredArgsConstructor
public class CustomerCouponController {

    private final CustomerCouponService customerCouponService;

    @GetMapping("/available")
    public ResponseEntity<List<BusinessWithPartnersCouponsDTO>> getAvailableCoupons(
            @AuthenticationPrincipal CustomUserDetails user) {
        Long customerId = user.getUserId();
        List<BusinessWithPartnersCouponsDTO> coupons = customerCouponService.getAvailableCouponsGrouped(customerId);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/received")
    public ResponseEntity<List<CouponResponseDTO>> getReceivedCoupons(@AuthenticationPrincipal CustomUserDetails user) {
        Long customerId = user.getUserId();
        List<CouponResponseDTO> coupons = customerCouponService.getReceivedCoupons(customerId);
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/{templateId}/claim")
    public ResponseEntity<CouponResponseDTO> claimCoupon(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("templateId") Long couponTemplateId) {
        Long customerId = user.getUserId();
        CouponResponseDTO newCoupon = customerCouponService.claimCoupon(customerId, couponTemplateId);
        return ResponseEntity.ok(newCoupon);
    }

    @GetMapping(path = "/{couponId}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getCouponQrCode(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long couponId) {
        try {
            byte[] qrCodeImage = customerCouponService.generateCouponQrCode(user.getUserId(), couponId);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCodeImage);
        } catch (Exception e) {
            log.error("Error generating QR code for coupon: {}", couponId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{couponId}/use")
    public ResponseEntity<CouponResponseDTO> useCoupon(@AuthenticationPrincipal CustomUserDetails user,
                                                       @PathVariable Long couponId) {
        // staffBusinessId: 쿠폰을 사용하는 매장 ID (직원 확인 시 전달)
        CouponResponseDTO updatedCoupon = customerCouponService.useCoupon(user.getUserId(),couponId);
        return ResponseEntity.ok(updatedCoupon);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<ExpiringCouponDTO>> getExpiringCoupons(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        Long customerId = user.getUserId();
        List<ExpiringCouponDTO> coupons = customerCouponService.getExpiringCoupons(customerId, limit);
        return ResponseEntity.ok(coupons);
    }
}

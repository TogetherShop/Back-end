package com.togethershop.backend.controller;

import com.togethershop.backend.dto.CouponIssueRequestDTO;
import com.togethershop.backend.dto.CouponResponseDTO;
import com.togethershop.backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public ResponseEntity<CouponResponseDTO> issueCoupon(@RequestBody CouponIssueRequestDTO requestDto) {
        try {
            log.info(requestDto.toString());
            CouponResponseDTO dto = couponService.issueCoupon(requestDto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error issuing coupon", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/read")
    public ResponseEntity<CouponResponseDTO> readCoupon(@RequestParam String couponCode) {
        try {
            CouponResponseDTO dto = couponService.readCoupon(couponCode);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Coupon not found: {}", couponCode, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getCouponQrCode(@RequestParam String couponCode) {
        try {
            byte[] qrCodeImage = couponService.generateCouponQrCode(couponCode);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCodeImage);
        } catch (Exception e) {
            log.error("Error generating QR code for coupon: {}", couponCode, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/use")
    public ResponseEntity<CouponResponseDTO> useCoupon(@RequestParam String couponCode, @RequestParam String jti) {
        try {
            CouponResponseDTO dto = couponService.useCoupon(couponCode, jti);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error using coupon: {}, jti: {}", couponCode, jti, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<CouponResponseDTO> cancelCouponUse(@RequestParam String couponCode) {
        try {
            CouponResponseDTO dto = couponService.cancelCouponUse(couponCode);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error cancelling coupon usage: {}", couponCode, e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}

package com.togethershop.backend.controller;

import com.togethershop.backend.dto.CouponIssueRequestDTO;
import com.togethershop.backend.dto.CouponResponseDTO;
import com.togethershop.backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping(value = "/issue", consumes = "application/json")
    public ResponseEntity<CouponResponseDTO> issueCoupon(@RequestBody CouponIssueRequestDTO requestDto) {
        try {
            var dto = couponService.issueCoupon(requestDto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/read")
    public ResponseEntity<CouponResponseDTO> readCoupon(@RequestParam String couponCode) {
        try {
            var dto = couponService.readCoupon(couponCode);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getCouponQrCode(@RequestParam String couponCode) {
        try {
            byte[] qrCodeImage = couponService.generateCouponQrCode(couponCode);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCodeImage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/use")
    public ResponseEntity<CouponResponseDTO> useCoupon(@RequestParam String couponCode, @RequestParam String jti) {
        try {
            var dto = couponService.useCoupon(couponCode, jti);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<CouponResponseDTO> cancelCouponUse(@RequestParam String couponCode) {
        try {
            var dto = couponService.cancelCouponUse(couponCode);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
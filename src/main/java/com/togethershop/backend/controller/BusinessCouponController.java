package com.togethershop.backend.controller;

import com.togethershop.backend.dto.*;
import com.togethershop.backend.service.BusinessCouponService;
import com.togethershop.backend.security.CustomUserDetails; // 커스텀 UserDetails
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/business/coupons")
@RequiredArgsConstructor
public class BusinessCouponController {

    private final BusinessCouponService businessCouponService;

    @GetMapping
    public ResponseEntity<BusinessCouponListResponseDTO> getBusinessCoupons(Authentication authentication) {
        try {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = user.getId(); // 숫자로 안전하게 가져오기
            BusinessCouponListResponseDTO response = businessCouponService.getBusinessCoupons(businessId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching business coupons", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/templates/{templateId}/analysis")
    public ResponseEntity<CouponAnalysisResponseDTO> getCouponAnalysis(
            @PathVariable Long templateId,
            Authentication authentication) { // 임시로 하드코딩
        try {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = user.getId(); // 숫자로 안전하게 가져오기
            CouponAnalysisResponseDTO analysis = businessCouponService.getCouponAnalysis(businessId, templateId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error fetching coupon analysis", e);
            return ResponseEntity.badRequest().build();
        }
    }
}

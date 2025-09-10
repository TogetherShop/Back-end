package com.togethershop.backend.controller;


import org.springframework.http.ResponseEntity;
import java.util.List;
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

    /**
     * 특정 사업자가 발급한 쿠폰 리스트 조회 (최신순)
     * @param businessId 사업자 ID
     * @param limit 조회할 개수 (기본값: 전체)
     * @return 쿠폰 템플릿 리스트
     */
    @GetMapping("/{businessId}")
    public ResponseEntity<List<CouponTemplateDTO>> getBusinessCouponslist(
            @PathVariable Long businessId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        
        log.info("사업자 ID: {} 쿠폰 조회 요청, 제한: {}", businessId, limit);
        
        List<CouponTemplateDTO> coupons = businessCouponService.getBusinessCouponslist(businessId, limit);
        
        log.info("사업자 ID: {} 쿠폰 조회 완료, 개수: {}", businessId, coupons.size());
        
        return ResponseEntity.ok(coupons);
    }

    /**
     * 특정 사업자의 최신 쿠폰 1개 조회 (매장 카드용)
     * @param businessId 사업자 ID
     * @return 최신 쿠폰 템플릿
     */
    @GetMapping("/{businessId}/latest")
    public ResponseEntity<CouponTemplateDTO> getLatestBusinessCoupon(@PathVariable Long businessId) {
        
        log.info("사업자 ID: {} 최신 쿠폰 조회 요청", businessId);
        
        CouponTemplateDTO latestCoupon = businessCouponService.getLatestBusinessCoupon(businessId);
        
        if (latestCoupon != null) {
            log.info("사업자 ID: {} 최신 쿠폰 조회 완료", businessId);
            return ResponseEntity.ok(latestCoupon);
        } else {
            log.info("사업자 ID: {} 쿠폰이 없음", businessId);
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * 특정 사업자의 최근 3개 쿠폰 조회 (상세 모달용)
     * @param businessId 사업자 ID
     * @return 최근 3개 쿠폰 템플릿 리스트
     */
    @GetMapping("/{businessId}/recent")
    public ResponseEntity<List<CouponTemplateDTO>> getRecentBusinessCoupons(@PathVariable Long businessId) {
        
        log.info("사업자 ID: {} 최근 3개 쿠폰 조회 요청", businessId);
        
        List<CouponTemplateDTO> recentCoupons = businessCouponService.getBusinessCoupons(businessId, 3);
        
        log.info("사업자 ID: {} 최근 쿠폰 조회 완료, 개수: {}", businessId, recentCoupons.size());
        
        return ResponseEntity.ok(recentCoupons);
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

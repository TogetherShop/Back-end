package com.togethershop.backend.service;

import com.togethershop.backend.dto.*;

public interface BusinessCouponService {
    BusinessCouponListResponseDTO getBusinessCoupons(Long businessId);
    CouponAnalysisResponseDTO getCouponAnalysis(Long businessId, Long templateId);
}
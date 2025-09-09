package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.CouponTemplate;
import com.togethershop.backend.dto.CouponTemplateDTO;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.CouponTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessCouponService {

    private final CouponTemplateRepository couponTemplateRepository;
    private final BusinessRepository businessRepository;

    /**
     * 특정 사업자의 applicable_business_id와 일치하는 쿠폰 리스트 조회
     * @param businessId 사업자 ID (applicable_business_id와 매칭)
     * @param limit 조회할 개수 (null이면 전체)
     * @return 쿠폰 템플릿 DTO 리스트 (description: "아메리카노 15%" 형식)
     */
    @Transactional(readOnly = true)
    public List<CouponTemplateDTO> getBusinessCoupons(Long businessId, Integer limit) {
        log.info("사업자 ID: {} 적용 가능한 쿠폰 조회 시작, 제한: {}", businessId, limit);

        // 사업자 정보 조회
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("사업자를 찾을 수 없습니다. ID: " + businessId));

        // applicable_business_id가 businessId와 일치하는 쿠폰 템플릿 조회
        List<CouponTemplate> couponTemplates;
        if (limit != null && limit > 0) {
            couponTemplates = couponTemplateRepository.findByBusinessIdOrderByCreatedAtDesc(
                    businessId, PageRequest.of(0, limit));
        } else {
            couponTemplates = couponTemplateRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        }

        log.info("사업자 ID: {} 적용 가능한 쿠폰 템플릿 개수: {}", businessId, couponTemplates.size());

        // DTO 변환
        return couponTemplates.stream()
                .map(template -> toCouponTemplateDTO(template, business))
                .collect(Collectors.toList());
    }

    /**
     * 특정 사업자의 최신 쿠폰 1개 조회
     * @param businessId 사업자 ID
     * @return 최신 쿠폰 템플릿 DTO (없으면 null)
     */
    @Transactional(readOnly = true)
    public CouponTemplateDTO getLatestBusinessCoupon(Long businessId) {
        log.info("사업자 ID: {} 최신 쿠폰 조회 시작", businessId);

        List<CouponTemplateDTO> coupons = getBusinessCoupons(businessId, 1);
        
        if (coupons.isEmpty()) {
            log.info("사업자 ID: {} 쿠폰이 없음", businessId);
            return null;
        }

        log.info("사업자 ID: {} 최신 쿠폰 조회 완료", businessId);
        return coupons.get(0);
    }

    /**
     * CouponTemplate -> CouponTemplateDTO 변환
     * description 형식: "아메리카노 15%" (item + discountValue%)
     */
    private CouponTemplateDTO toCouponTemplateDTO(CouponTemplate template, Business business) {
        // description 형식: "아메리카노 15%"
        String description = (template.getItem() != null ? template.getItem() : "상품") + " " + template.getDiscountValue() + "%";
        
        return CouponTemplateDTO.builder()
                .templateId(template.getId())
                .discountValue(template.getDiscountValue())
                .totalQuantity(template.getTotalQuantity())
                .currentQuantity(template.getCurrentQuantity())
                .createdAt(template.getCreatedAt())
                .applicableBusinessId(template.getApplicableBusinessId())
                .businessName(business.getBusinessName())
                .businessCategory(business.getBusinessCategory())
                .startDate(template.getStartDate())
                .endDate(template.getEndDate())
                .description(description)
                .build();
    }
}

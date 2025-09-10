package com.togethershop.backend.service;

import com.togethershop.backend.domain.*;
import com.togethershop.backend.dto.*;
import com.togethershop.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCouponService {

    private final CouponRepository couponRepository;
    private final QRCodeService qrCodeService;
    private final BusinessRepository businessRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final JwtJtiService jwtService;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PartnershipRepository partnershipRepository;
    private final CouponTemplateRepository couponTemplateRepository;

    @Transactional(readOnly = true)
    public List<BusinessWithPartnersCouponsDTO> getAvailableCouponsGrouped(Long customerId) {
        log.info("▶ 시작: 고객ID={} 최근 결제 매장 3곳 조회", customerId);

        // 1. 고객 최근 결제 매장 3곳 조회
        List<PaymentHistory> payments = paymentHistoryRepository.findTop3ByCustomerIdOrderByPaymentDateDesc(customerId);
        List<Long> recentBusinessIds = payments.stream()
                .map(PaymentHistory::getBusinessId)
                .distinct()
                .collect(Collectors.toList());

        log.info("최근 결제 매장 ID 리스트: {}", recentBusinessIds);

        if (recentBusinessIds.isEmpty()) {
            log.info("최근 방문 매장이 없습니다.");
            return Collections.emptyList();
        }

        // 2. 각 최근 방문 매장을 requester로 하는 partnership 조회
        List<Partnership> partnerships = partnershipRepository.findByRequester_IdIn(recentBusinessIds);
        log.info("조회된 partnership 개수: {}", partnerships.size());

        if (partnerships.isEmpty()) {
            log.info("제휴 매장이 없습니다.");
            return Collections.emptyList();
        }

        // 3. partnershipId 리스트 수집
        List<Long> partnershipIds = partnerships.stream()
                .map(Partnership::getId)
                .collect(Collectors.toList());
        log.info("파트너십 ID 리스트: {}", partnershipIds);

        // 4. partnershipId로 coupon_templates 조회
        List<CouponTemplate> couponTemplates = couponTemplateRepository.findByPartnership_IdIn(partnershipIds);
        log.info("조회된 couponTemplates 개수: {}", couponTemplates.size());

        if (couponTemplates.isEmpty()) {
            log.info("쿠폰 템플릿이 없습니다.");
            return Collections.emptyList();
        }

        // 5. partnershipId -> Partnership map 생성
        Map<Long, Partnership> partnershipMap = partnerships.stream()
                .collect(Collectors.toMap(Partnership::getId, p -> p));
        log.debug("partnershipMap 크기: {}", partnershipMap.size());

        // 6. partnerBusinessId 리스트 추출 및 캐싱
        Set<Long> partnerBusinessIds = partnerships.stream()
                .map(p -> p.getPartner().getId())
                .collect(Collectors.toSet());
        log.info("파트너 사업장 ID 리스트: {}", partnerBusinessIds);

        Map<Long, Business> partnerBusinessMap = businessRepository.findAllById(partnerBusinessIds).stream()
                .collect(Collectors.toMap(Business::getId, b -> b));
        log.debug("partnerBusinessMap 크기: {}", partnerBusinessMap.size());

        // 7. 결과 DTO 생성
        List<BusinessWithPartnersCouponsDTO> result = new ArrayList<>();

        // 최근 방문 매장별로 처리
        for (Long recentBusinessId : recentBusinessIds) {
            log.info("▶ 처리 시작: 최근 방문 매장 ID = {}", recentBusinessId);

            List<PartnerCouponsDTO> partnerCouponsList = new ArrayList<>();

            // 해당 매장과 연관된 partnership 필터링
            List<Partnership> relatedPartnerships = partnerships.stream()
                    .filter(p -> p.getRequester().getId().equals(recentBusinessId))
                    .toList();
            log.info("  관련 partnership 개수: {}", relatedPartnerships.size());

            for (Partnership partnership : relatedPartnerships) {
                Long partnerId = partnership.getPartner().getId();
                Business partnerBusiness = partnerBusinessMap.get(partnerId);

                if (partnerBusiness == null) {
                    log.warn("  파트너 사업장 ID={}에 대한 정보를 찾을 수 없음", partnerId);
                    continue;
                }

                // 쿠폰 필터링 : partnershipId 및 applicableBusinessId 기준
                List<CouponTemplate> filteredCoupons = couponTemplates.stream()
                        .filter(ct -> ct.getApplicableBusinessId().equals(partnership.getPartner().getId()))
                        .toList();

                log.info("    partnershipId={} 필터링된 쿠폰 개수: {}", partnership.getId(), filteredCoupons.size());

                if (filteredCoupons.isEmpty()) {
                    log.info("    조건에 맞는 쿠폰 템플릿 없음, 다음 제휴로 이동");
                    continue;
                }

                List<CouponTemplateDTO> couponDTOs = filteredCoupons.stream()
                        .map(ct -> toCouponTemplateDTO(ct, partnerBusiness))
                        .toList();

                partnerCouponsList.add(new PartnerCouponsDTO(toBusinessDTO(partnerBusiness), couponDTOs));
            }

            // 방문 매장 정보 조회 및 DTO 변환
            Business visitBusiness = businessRepository.findById(recentBusinessId).orElse(null);
            if (visitBusiness != null) {
                result.add(new BusinessWithPartnersCouponsDTO(toBusinessDTO(visitBusiness), partnerCouponsList));
                log.info("  매장 ID={} 처리 완료, 파트너 쿠폰 그룹 수: {}", recentBusinessId, partnerCouponsList.size());
            } else {
                log.warn("  방문 매장 ID={} 정보 없음", recentBusinessId);
            }
        }

        log.info("▶ 전체 처리 완료, 매장 수: {}, 총 파트너 쿠폰 그룹 수: {}",
                result.size(),
                result.stream().mapToInt(b -> b.getCouponsByPartners().size()).sum());

        return result;
    }


    private CouponTemplateDTO toCouponTemplateDTO(CouponTemplate template, Business partnerBusiness) {
        return CouponTemplateDTO.builder()
                .templateId(template.getId())
                .discountValue(template.getDiscountValue())
                .totalQuantity(template.getTotalQuantity())
                .currentQuantity(template.getCurrentQuantity())
                .createdAt(template.getCreatedAt())
                .applicableBusinessId(template.getApplicableBusinessId())
                .businessName(partnerBusiness.getBusinessName())
                .businessCategory(partnerBusiness.getBusinessCategory())
                .startDate(template.getStartDate())
                .endDate(template.getEndDate())
                .description(template.getDiscountValue() + "%" + template.getItem() + " 할인")
                .build();
    }


    // Business -> BusinessDTO 변환 메서드
    private BusinessDTO toBusinessDTO(Business business) {
        if (business == null) return null;
        return BusinessDTO.builder()
                .businessId(business.getId())
                .businessName(business.getBusinessName())
                .businessCategory(business.getBusinessCategory())
                .phoneNumber(business.getPhoneNumber())
                .address(business.getAddress())
                .build();
    }


    public List<CouponResponseDTO> getReceivedCoupons(Long customerId) {
        // 1. 고객 쿠폰 조회(status = ISSUED)
        List<Coupon> coupons = couponRepository.findByCustomerIdAndStatus(customerId, CouponStatus.ISSUED);

        if (coupons.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. coupon_template id 리스트 수집
        Set<Long> templateIds = coupons.stream()
                .map(Coupon::getTemplateId)
                .collect(Collectors.toSet());

        // 3. coupon_template 조회 (partnership 포함, LAZY면 fetch join 또는 별도 조회 필요)
        List<CouponTemplate> templates = couponTemplateRepository.findAllById(templateIds);
        Map<Long, CouponTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(CouponTemplate::getId, t -> t));

        // 4. partnershipId로 파트너 businessId 조회 (couponTemplate -> partnership -> partnerBusiness)
        Set<Long> partnerBusinessIds = templates.stream()
                .map(t -> t.getPartnership().getPartner().getId())
                .collect(Collectors.toSet());

        // 5. partner business 조회
        List<Business> partnerBusinesses = businessRepository.findAllById(partnerBusinessIds);
        Map<Long, Business> businessMap = partnerBusinesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        // 6. DTO 변환
        return coupons.stream()
                .map(c -> {
                    CouponTemplate template = templateMap.get(c.getTemplateId());
                    if (template == null) return null;

                    Long partnerBusinessId = template.getPartnership().getPartner().getId();
                    Business partnerBusiness = businessMap.get(partnerBusinessId);

                    // 설명은 기존과 동일하게 할인율+품목 조합으로 생성
                    String description = template.getDiscountValue() + "%" + template.getItem() + " 할인";

                    return CouponResponseDTO.builder()
                            .couponId(c.getCouponId())
                            .templateId(c.getTemplateId())
                            .couponCode(c.getCouponCode())
                            .qrCodeData(c.getQrCodeData())
                            .pinCode(c.getPinCode())
                            .issueDate(c.getIssueDate())
                            .expireDate(c.getExpireDate())
                            .usedDate(c.getUsedDate())
                            .status(c.getStatus().name())
                            .description(description)
                            .businessName(partnerBusiness != null ? partnerBusiness.getBusinessName() : null)
                            .businessCategory(partnerBusiness != null ? partnerBusiness.getBusinessCategory() : null)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    @Transactional
    public CouponResponseDTO claimCoupon(Long customerId, Long couponTemplateId) {
        // 쿠폰템플릿 조회
        CouponTemplate template = couponTemplateRepository.findById(couponTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon template id"));

        // 쿠폰 발급 조건 검증 예: maxIssueCount, maxUsePerCustomer, isActive 체크 추가 가능
        // totalQuantity 검증 및 감소
        Integer currentQuantity = template.getCurrentQuantity();
        if (currentQuantity == null || currentQuantity <= 0) {
            throw new IllegalStateException("Coupon template is out of stock");
        }
        template.setCurrentQuantity(currentQuantity - 1);
        couponTemplateRepository.save(template);

        // 쿠폰 발급용 couponCode 및 JWT JTI 생성 (예시 UUID 활용)
        String couponCode = "CPN" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
        String jtiToken = UUID.randomUUID().toString();

        Coupon coupon = Coupon.builder()
                .templateId(template.getId())
                .customerId(customerId)
                .couponCode(couponCode)
                .jtiToken(jtiToken)
                .qrCodeData(null)  // QR코드는 필요시 별도 생성
                .pinCode(null)     // PIN 필요시 별도 생성
                .issueDate(LocalDateTime.now())
                .expireDate(LocalDateTime.now().plusDays(template.getEndDate().getDayOfMonth()))
                .status(CouponStatus.ISSUED)
                .build();

        couponRepository.save(coupon);

        // 엔티티 -> DTO 변환
        return CouponResponseDTO.builder()
                .couponId(coupon.getCouponId())
                .templateId(coupon.getTemplateId())
                .couponCode(coupon.getCouponCode())
                .qrCodeData(coupon.getQrCodeData())
                .pinCode(coupon.getPinCode())
                .issueDate(coupon.getIssueDate())
                .expireDate(coupon.getExpireDate())
                .usedDate(coupon.getUsedDate())
                .status(coupon.getStatus().name())
                .build();
    }


    @Transactional
    public byte[] generateCouponQrCode(Long userId, Long couponId) throws Exception {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getCustomerId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to coupon");
        }
        if (coupon.getStatus() != CouponStatus.ISSUED) {
            throw new RuntimeException("Coupon is not valid for QR code generation");
        }
        if (coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon expired");
        }

        String jwtToken = jwtService.generateTokenWithJti(coupon.getCouponCode(), coupon.getJtiToken());
        return qrCodeService.generateQRCode(jwtToken);
    }


    @Transactional
    public CouponResponseDTO useCoupon(Long customerId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Unauthorized access to coupon");
        }

        if (coupon.getStatus() != CouponStatus.ISSUED) {
            throw new RuntimeException("Coupon cannot be used. Current status: " + coupon.getStatus());
        }

        if (coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon expired");
        }

        coupon.setStatus(CouponStatus.USED);
        coupon.setUsedDate(LocalDateTime.now());

        couponRepository.save(coupon);

        return CouponResponseDTO.builder()
                .couponId(coupon.getCouponId())
                .templateId(coupon.getTemplateId())
                .couponCode(coupon.getCouponCode())
                .qrCodeData(coupon.getQrCodeData())
                .pinCode(coupon.getPinCode())
                .issueDate(coupon.getIssueDate())
                .expireDate(coupon.getExpireDate())
                .usedDate(coupon.getUsedDate())
                .status(coupon.getStatus().name())
                .build();
    }

    public List<ExpiringCouponDTO> getExpiringCoupons(Long customerId, int limit) {
        LocalDateTime now = LocalDateTime.now();
        log.info("▶ 시작: 고객ID={}, 만료 임박 쿠폰 최대 조회 개수={}", customerId, limit);

        // 1. 고객 쿠폰 중 만료 임박 쿠폰 조회 (status = ISSUED 필터 포함 가정)
        List<Coupon> coupons = couponRepository.findExpiringCoupons(customerId, now, PageRequest.of(0, limit));
        log.info("조회된 만료 임박 쿠폰 개수: {}", coupons.size());

        if (coupons.isEmpty()) {
            log.info("만료 임박 쿠폰이 없습니다.");
            return List.of();
        }

        // 2. coupon_template id 리스트 수집
        List<Long> templateIds = coupons.stream()
                .map(Coupon::getTemplateId)
                .distinct()
                .collect(Collectors.toList());
        log.info("관련 coupon_template ID 리스트: {}", templateIds);

        // 3. coupon_template 조회 (partnership 포함)
        List<CouponTemplate> templates = couponTemplateRepository.findAllById(templateIds);
        log.info("조회된 coupon_template 개수: {}", templates.size());

        Map<Long, CouponTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(CouponTemplate::getId, t -> t));

        // 4. coupon_template의 partnerBusinessId 추출 (partnership -> partner)
        Set<Long> partnerBusinessIds = templates.stream()
                .map(t -> {
                    Long partnerId = null;
                    try {
                        partnerId = t.getPartnership().getPartner().getId();
                    } catch (Exception e) {
                        log.warn("쿠폰템플릿 ID {} 에서 partnerBusinessId 조회 실패: {}", t.getId(), e.getMessage());
                    }
                    return partnerId;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        log.info("partnerBusinessId(파트너 사업장) 리스트: {}", partnerBusinessIds);

        // 5. 파트너 사업장 조회
        List<Business> businesses = businessRepository.findAllById(partnerBusinessIds);
        log.info("조회된 파트너 사업장 개수: {}", businesses.size());

        Map<Long, Business> businessMap = businesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        // 6. DTO 변환
        List<ExpiringCouponDTO> result = coupons.stream()
                .map(coupon -> {
                    CouponTemplate template = templateMap.get(coupon.getTemplateId());
                    if (template == null) {
                        log.warn("coupon_template ID {} 가 조회되지 않음", coupon.getTemplateId());
                        return null;
                    }

                    Business partnerBusiness = null;
                    try {
                        partnerBusiness = businessMap.get(template.getPartnership().getPartner().getId());
                    } catch (Exception e) {
                        log.warn("쿠폰템플릿 ID {} 에 대한 partnerBusiness 조회 실패: {}", template.getId(), e.getMessage());
                    }

                    int daysLeft = (int) Duration.between(now, coupon.getExpireDate()).toDays();

                    log.debug("쿠폰 ID {}: 만료까지 {}일, partnerBusinessId={}, 사업장명={}", coupon.getCouponId(), daysLeft,
                            partnerBusiness != null ? partnerBusiness.getId() : null,
                            partnerBusiness != null ? partnerBusiness.getBusinessName() : null);

                    return ExpiringCouponDTO.builder()
                            .couponId(coupon.getCouponId())
                            .couponCode(coupon.getCouponCode())
                            .expireDate(coupon.getExpireDate())
                            .templateId(coupon.getTemplateId())
                            .discountValue(template.getDiscountValue())
                            .businessName(partnerBusiness != null ? partnerBusiness.getBusinessName() : null)
                            .businessCategory(partnerBusiness != null ? partnerBusiness.getBusinessCategory() : null)
                            .daysLeft(daysLeft)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("▶ 만료 임박 쿠폰 조회 완료, 반환 쿠폰 개수: {}", result.size());
        return result;
    }


    //오류 방지용 테스트
    public void issueMutualCoupons(String roomId, Long proposerId, Long accepterId, Map<String, Object> proposalPayload) {
        Long couponTemplateId = parseCouponTemplateId(proposalPayload);

        claimCoupon(proposerId, couponTemplateId);
        claimCoupon(accepterId, couponTemplateId);

        // 필요에 따라 roomId, proposalPayload 추가 처리 가능
    }

    private Long parseCouponTemplateId(Map<String, Object> proposalPayload) {
        Object idObj = proposalPayload.get("couponTemplateId"); // 키 이름은 실제 구조에 맞게 수정
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        } else if (idObj instanceof String) {
            return Long.parseLong((String) idObj);
        } else {
            throw new IllegalArgumentException("Invalid couponTemplateId in proposalPayload");
        }
    }


}

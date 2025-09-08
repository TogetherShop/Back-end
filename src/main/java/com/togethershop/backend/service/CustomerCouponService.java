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

    public List<BusinessWithPartnersCouponsDTO> getAvailableCouponsGrouped(Long customerId) {
        log.info("▶ 시작: 고객ID={} 최근 결제 매장 3곳 조회", customerId);

        // 1. 최근 결제 매장 3개 조회
        List<PaymentHistory> payments = paymentHistoryRepository.findTop3ByCustomerIdOrderByPaymentDateDesc(customerId);
        List<Long> recentBusinessIds = payments.stream()
                .map(PaymentHistory::getBusinessId)
                .distinct()
                .collect(Collectors.toList());

        log.info("최근 결제한 매장 ID 리스트: {}", recentBusinessIds);

        if (recentBusinessIds.isEmpty()) {
            log.info("최근 결제한 매장이 없습니다.");
            return Collections.emptyList();
        }

        List<Business> recentBusinesses = businessRepository.findAllById(recentBusinessIds);
        Map<Long, Business> businessMap = recentBusinesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        List<BusinessWithPartnersCouponsDTO> result = new ArrayList<>();

        // 2. 매장별 제휴 및 쿠폰 조회
        for (Business business : recentBusinesses) {
            log.info("▶ 처리 중 매장: {} (ID: {})", business.getBusinessName(), business.getId());

            List<Partnership> partnerships = partnershipRepository.findByRequester_IdAndStatus(business.getId(), PartnershipStatus.ACCEPTED);
            log.info("  제휴 매장 수: {}", partnerships.size());

            List<PartnerCouponsDTO> partnerCouponsList = new ArrayList<>();

            for (Partnership partnership : partnerships) {
                Long partnershipId = partnership.getPartnershipId();
                log.info("    제휴 partnershipId: {}", partnershipId);

                List<ChatRoom> chatRooms = chatRoomRepository.findByPartnershipIdIn(Collections.singletonList(partnershipId));
                log.info("      chatRooms 수: {}", chatRooms.size());

                List<Long> roomIds = chatRooms.stream()
                        .map(cr -> {
                            try {
                                return cr.getId();
                            } catch (Exception e) {
                                log.warn("      roomId 파싱 실패, roomId={}", cr.getRoomId());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (roomIds.isEmpty()) {
                    log.info("      유효한 roomId 없음, 다음 제휴로 이동");
                    continue;
                }

                List<CouponTemplate> couponTemplates = couponTemplateRepository.findByRoomIdInAndIsActive(roomIds, true);
                log.info("      쿠폰 템플릿 수: {}", couponTemplates.size());

                if (couponTemplates.isEmpty()) {
                    continue;
                }

                Business partnerBusiness = businessMap.get(partnership.getPartner().getId());
                BusinessDTO partnerBusinessDTO = toBusinessDTO(partnerBusiness);

                List<CouponTemplateDTO> couponDTOs = couponTemplates.stream()
                        .map(template -> toCouponTemplateDTO(template, partnerBusiness))
                        .collect(Collectors.toList());

                partnerCouponsList.add(new PartnerCouponsDTO(partnerBusinessDTO, couponDTOs));
            }

            BusinessDTO businessDTO = toBusinessDTO(business);
            result.add(new BusinessWithPartnersCouponsDTO(businessDTO, partnerCouponsList));
        }

        log.info("▶ 완료: 총 매장 수 {}, 총 응답 크기 {}", result.size(), result.stream().mapToInt(b -> b.getCouponsByPartners().size()).sum());
        return result;
    }

    private CouponTemplateDTO toCouponTemplateDTO(CouponTemplate template, Business business) {
        return CouponTemplateDTO.builder()
                .templateId(template.getTemplateId())
                .discountValue(template.getDiscountValue())
                .totalQuantity(template.getTotalQuantity())
                .currentQuantity(template.getCurrentQuantity())
                .maxUsePerCustomer(template.getMaxUsePerCustomer())
                .isActive(template.getIsActive())
                .createdAt(template.getCreatedAt())
                .roomId(template.getRoomId())
                .businessId(template.getBusinessId())
                .businessName(business != null ? business.getBusinessName() : null)
                .businessCategory(business != null ? business.getBusinessCategory() : null)
                .startDate(template.getStartDate())
                .endDate(template.getEndDate())
                .description(template.getDescription())
                .termsAndConditions(template.getTermsAndConditions())
                .acceptedByRequester(template.getAcceptedByRequester())
                .acceptedByRecipient(template.getAcceptedByRecipient())
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
        // 1. 고객 쿠폰 조회 (status = ISSUED)
        List<Coupon> coupons = couponRepository.findByCustomerIdAndStatus(customerId, CouponStatus.ISSUED);

        if (coupons.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. coupon_template id 리스트 수집
        Set<Long> templateIds = coupons.stream()
                .map(Coupon::getTemplateId)
                .collect(Collectors.toSet());

        // 3. coupon_template 조회 (description 포함)
        List<CouponTemplate> templates = couponTemplateRepository.findAllById(templateIds);
        Map<Long, CouponTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(CouponTemplate::getTemplateId, t -> t));

        // 4. coupon_template 에서 참조하는 business_id 수집하여 business 조회
        Set<Long> businessIds = templates.stream()
                .map(CouponTemplate::getBusinessId)
                .collect(Collectors.toSet());
        List<Business> businesses = businessRepository.findAllById(businessIds);
        Map<Long, Business> businessMap = businesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        // 5. DTO 변환
        return coupons.stream()
                .map(c -> {
                    CouponTemplate template = templateMap.get(c.getTemplateId());
                    Business business = (template != null) ? businessMap.get(template.getBusinessId()) : null;

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
                            .description(template != null ? template.getDescription() : null)
                            .businessName(business != null ? business.getBusinessName() : null)
                            .businessCategory(business != null ? business.getBusinessCategory() : null)
                            .build();
                })
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
                .templateId(template.getTemplateId())
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

        List<Coupon> coupons = couponRepository.findExpiringCoupons(customerId, now, PageRequest.of(0, limit));

        if (coupons.isEmpty()) {
            return List.of();
        }

        List<Long> templateIds = coupons.stream()
                .map(Coupon::getTemplateId)
                .distinct()
                .collect(Collectors.toList());

        List<CouponTemplate> templates = couponTemplateRepository.findAllById(templateIds);

        var templateMap = templates.stream()
                .collect(Collectors.toMap(CouponTemplate::getTemplateId, t -> t));

        var businessIds = templates.stream()
                .map(CouponTemplate::getBusinessId)
                .distinct()
                .collect(Collectors.toList());

        var businesses = businessRepository.findAllById(businessIds);

        var businessMap = businesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        return coupons.stream()
                .map(coupon -> {
                    CouponTemplate template = templateMap.get(coupon.getTemplateId());
                    Business business = template != null ? businessMap.get(template.getBusinessId()) : null;

                    int daysLeft = (int) Duration.between(now, coupon.getExpireDate()).toDays();

                    return ExpiringCouponDTO.builder()
                            .couponId(coupon.getCouponId())
                            .couponCode(coupon.getCouponCode())
                            .expireDate(coupon.getExpireDate())
                            .templateId(coupon.getTemplateId())
                            .discountValue(template != null ? template.getDiscountValue() : null)
                            .businessName(business != null ? business.getBusinessName() : null)
                            .businessCategory(business != null ? business.getBusinessCategory() : null)
                            .daysLeft(daysLeft)
                            .build();
                })
                .collect(Collectors.toList());

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

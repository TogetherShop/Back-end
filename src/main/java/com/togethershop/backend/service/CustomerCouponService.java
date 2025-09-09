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
        // 1. 최근 결제 매장 3곳 조회
        List<PaymentHistory> payments = paymentHistoryRepository.findTop3ByCustomerIdOrderByPaymentDateDesc(customerId);
        List<Long> recentBusinessIds = payments.stream()
                .map(PaymentHistory::getBusinessId)
                .distinct()
                .collect(Collectors.toList());

        if (recentBusinessIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Business> recentBusinesses = businessRepository.findAllById(recentBusinessIds);
        Map<Long, Business> businessMap = recentBusinesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        List<BusinessWithPartnersCouponsDTO> result = new ArrayList<>();

        // 2. 최근 매장별 제휴 및 쿠폰 조회
        for (Business business : recentBusinesses) {
            // 본인이 requester 또는 partner로 속한 모든 제휴 조회
            List<Partnership> partnerships = partnershipRepository
                    .findByRequester_IdOrPartner_Id(business.getId(), business.getId());

            List<PartnerCouponsDTO> partnerCouponsList = new ArrayList<>();

            for (Partnership partnership : partnerships) {
                // 상대방 business 식별
                Business partner = partnership.getRequester().getId().equals(business.getId())
                        ? partnership.getPartner()
                        : partnership.getRequester();
                BusinessDTO partnerBusinessDTO = toBusinessDTO(partner);

                // ChatRoom 조건: partnershipId + status="COMPLETED"
                List<ChatRoom> chatRooms = chatRoomRepository
                        .findByPartnershipIdAndStatus(partnership.getId(), ChatStatus.ACCEPTED);

                List<Long> roomIds = chatRooms.stream()
                        .map(ChatRoom::getId)
                        .collect(Collectors.toList());
                if (roomIds.isEmpty()) continue;

                // 모든 룸의 쿠폰 템플릿(활성 조건 없음)
                List<CouponTemplate> couponTemplates =
                        couponTemplateRepository.findByRoomIdIn(roomIds);

                if (couponTemplates.isEmpty()) continue;

                List<CouponTemplateDTO> couponDTOs = couponTemplates.stream()
                        .map(template -> toCouponTemplateDTO(template, partner))
                        .collect(Collectors.toList());

                partnerCouponsList.add(new PartnerCouponsDTO(partnerBusinessDTO, couponDTOs));
            }

            BusinessDTO businessDTO = toBusinessDTO(business);
            result.add(new BusinessWithPartnersCouponsDTO(businessDTO, partnerCouponsList));
        }

        return result;
    }


    private CouponTemplateDTO toCouponTemplateDTO(CouponTemplate template, Business business) {
        return CouponTemplateDTO.builder()
                .templateId(template.getId())
                .discountValue(template.getDiscountValue())
                .totalQuantity(template.getTotalQuantity())
                .currentQuantity(template.getTotalQuantity())
                .createdAt(template.getCreatedAt())
                .roomId(template.getRoom().getId())
                .businessId(template.getPartnership().getPartner().getId())
                .businessName(business != null ? business.getBusinessName() : null)
                .businessCategory(business != null ? business.getBusinessCategory() : null)
                .startDate(template.getStartDate())
                .endDate(template.getEndDate())
                .description(template.getItem())
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
                .collect(Collectors.toMap(CouponTemplate::getId, t -> t));

        // 4. coupon_template 에서 참조하는 business_id 수집하여 business 조회
        Set<Long> businessIds = templates.stream()
                .map(CouponTemplate::getPartnership).map(Partnership::getPartner).map(Business::getId)
                .collect(Collectors.toSet());
        List<Business> businesses = businessRepository.findAllById(businessIds);
        Map<Long, Business> businessMap = businesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        // 5. DTO 변환
        return coupons.stream()
                .map(c -> {
                    CouponTemplate template = templateMap.get(c.getTemplateId());
                    Business business = (template != null) ? businessMap.get(template.getPartnership().getPartner().getId()) : null;

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
                            .description(template != null ? template.getItem() : null)
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
        Integer currentQuantity = template.getTotalQuantity();
        if (currentQuantity == null || currentQuantity <= 0) {
            throw new IllegalStateException("Coupon template is out of stock");
        }
        template.setTotalQuantity(currentQuantity - 1);
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
                .collect(Collectors.toMap(CouponTemplate::getId, t -> t));

        var businessIds = templates.stream()
                .map(CouponTemplate::getPartnership).map(Partnership::getPartner).map(Business::getId)
                .distinct()
                .collect(Collectors.toList());

        var businesses = businessRepository.findAllById(businessIds);

        var businessMap = businesses.stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        return coupons.stream()
                .map(coupon -> {
                    CouponTemplate template = templateMap.get(coupon.getTemplateId());
                    Business business = template != null ? businessMap.get(template.getPartnership().getPartner().getId()) : null;

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

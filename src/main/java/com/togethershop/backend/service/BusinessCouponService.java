package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.domain.CouponTemplate;
import com.togethershop.backend.dto.*;
import com.togethershop.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessCouponService {

    private final CouponTemplateRepository couponTemplateRepository;
    private final BusinessRepository businessRepository;
    private final CouponRepository couponRepository;
    private final PartnershipRepository partnershipRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 특정 사업자의 applicable_business_id와 일치하는 쿠폰 리스트 조회
     *
     * @param businessId 사업자 ID (applicable_business_id와 매칭)
     * @param limit      조회할 개수 (null이면 전체)
     * @return 쿠폰 템플릿 DTO 리스트 (description: "아메리카노 15%" 형식)
     */
    @Transactional(readOnly = true)
    public List<CouponTemplateDTO> getBusinessCouponslist(Long businessId, Integer limit) {
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
     *
     * @param businessId 사업자 ID
     * @return 최신 쿠폰 템플릿 DTO (없으면 null)
     */
    @Transactional(readOnly = true)
    public CouponTemplateDTO getLatestBusinessCoupon(Long businessId) {
        log.info("사업자 ID: {} 최신 쿠폰 조회 시작", businessId);

        List<CouponTemplateDTO> coupons = getBusinessCouponslist(businessId, 1);

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


    @Transactional(readOnly = true)
    public BusinessCouponListResponseDTO getBusinessCoupons(Long businessId) {
        try {
            List<BusinessCouponDTO> myCoupons = new ArrayList<>();
            List<BusinessCouponDTO> receivedCoupons = new ArrayList<>();

            // ACCEPTED 상태의 ChatRoom 조회
            List<ChatRoom> acceptedRooms = chatRoomRepository.findByBusinessIdAndStatus(
                    businessId, ChatStatus.ACCEPTED
            );

            for (ChatRoom room : acceptedRooms) {
                // 해당 room에 연결된 쿠폰 템플릿 조회
                List<CouponTemplate> roomTemplates = couponTemplateRepository.findByRoomId(room.getId());

                if (roomTemplates.isEmpty()) {
                    // 쿠폰 템플릿이 없는 경우 exchanging 상태로 생성
                    BusinessCouponDTO exchangingCoupon = createExchangingCoupon(room, businessId);
                    // exchanging 상태는 내가 발급한 쿠폰에만 추가
                    if (room.getRequester().getId().equals(businessId)) {
                        myCoupons.add(exchangingCoupon);
                    }
                    // receivedCoupons에는 exchanging 상태 추가하지 않음
                } else {
                    // 쿠폰 템플릿이 있는 경우 일반적인 처리
                    for (CouponTemplate template : roomTemplates) {
                        BusinessCouponDTO couponDTO = convertToBusinessCouponDTO(template, businessId);

                        if (template.getApplicableBusinessId().equals(businessId)) {
                            myCoupons.add(couponDTO);
                        } else {
                            // receivedCoupons에는 exchanging 상태가 아닌 것만 추가
                            if (!"exchanging".equals(couponDTO.getStatus())) {
                                receivedCoupons.add(couponDTO);
                            }
                        }
                    }
                }
            }

            // 내가 발급한 쿠폰 템플릿들 중 room이 없는 것들도 추가
            List<CouponTemplate> myTemplatesWithoutRoom = couponTemplateRepository.findByApplicableBusinessIdAndRoomIsNull(businessId);
            List<BusinessCouponDTO> myAdditionalCoupons = myTemplatesWithoutRoom.stream()
                    .map(template -> convertToBusinessCouponDTO(template, businessId))
                    .collect(Collectors.toList());
            myCoupons.addAll(myAdditionalCoupons);

            return BusinessCouponListResponseDTO.builder()
                    .success(true)
                    .myCoupons(myCoupons)
                    .receivedCoupons(receivedCoupons)
                    .message("Successfully fetched business coupons")
                    .build();

        } catch (Exception e) {
            log.error("Error fetching business coupons", e);
            return BusinessCouponListResponseDTO.builder()
                    .success(false)
                    .myCoupons(new ArrayList<>())
                    .receivedCoupons(new ArrayList<>())
                    .message("Failed to fetch coupons")
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public CouponAnalysisResponseDTO getCouponAnalysis(Long businessId, Long templateId) {
        try {
            CouponTemplate template = couponTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found"));

            // 쿠폰 통계 계산
            Long totalIssued = couponRepository.countByTemplateId(templateId);
            Long totalUsed = couponRepository.countByTemplateIdAndStatus(templateId, CouponStatus.USED);

            double usageRate = totalIssued > 0 ? (double) totalUsed / totalIssued * 100 : 0;

            // 오늘부터 30일 전까지의 기간 설정
            LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            LocalDateTime startDate = endDate.minusDays(29).withHour(0).withMinute(0).withSecond(0);

            log.info("Analysis date range: {} to {}", startDate, endDate);

            // 일별 사용 통계 (최근 30일)
            List<CouponAnalysisResponseDTO.DailyUsageDTO> dailyUsage = calculateDailyUsageFromDB(templateId, startDate, endDate);

            // 일별 누적 통계 (최근 30일)
            List<CouponAnalysisResponseDTO.DailyCumulativeDTO> dailyCumulative = calculateDailyCumulativeFromDB(templateId, startDate, endDate);

            // 총 수익 계산
            BigDecimal totalRevenue = BigDecimal.valueOf(totalUsed * template.getDiscountValue());

            // 🔹 새롭게 추가: templateId 기준 상세 정보
            BusinessCouponDTO couponDetail = convertToBusinessCoupon(template, businessId);

            return CouponAnalysisResponseDTO.builder()
                    .success(true)
                    .templateId(templateId)
                    .description(template.getItem())
                    .totalIssued(totalIssued.intValue())
                    .totalUsed(totalUsed.intValue())
                    .usageRate(Math.round(usageRate * 10.0) / 10.0)
                    .totalRevenue(totalRevenue)
                    .dailyUsage(dailyUsage)
                    .dailyCumulative(dailyCumulative)
                    .currentQuantity(template.getCurrentQuantity())
                    .totalQuantity(template.getTotalQuantity())
                    .termsAndConditions("이용약관이 적용됩니다.")
                    .couponDetail(couponDetail) // 여기 추가
                    .message("Successfully fetched coupon analysis")
                    .build();

        } catch (Exception e) {
            log.error("Error fetching coupon analysis for templateId: " + templateId, e);
            return CouponAnalysisResponseDTO.builder()
                    .success(false)
                    .templateId(templateId)
                    .message("Failed to fetch coupon analysis: " + e.getMessage())
                    .build();
        }
    }

    private List<CouponAnalysisResponseDTO.DailyUsageDTO> calculateDailyUsageFromDB(Long templateId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating daily usage for template {} from {} to {}", templateId, startDate, endDate);

        List<Object[]> results = couponRepository.findDailyUsageStats(templateId, startDate, endDate);
        log.info("Found {} daily usage records from DB", results.size());

        // DB 결과를 맵으로 변환
        Map<LocalDate, Integer> usageMap = new HashMap<>();
        for (Object[] result : results) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            Integer count = ((Number) result[1]).intValue();
            usageMap.put(date, count);
        }

        // 30일 전체 기간에 대해 데이터 생성 (없는 날은 0으로)
        List<CouponAnalysisResponseDTO.DailyUsageDTO> dailyUsage = new ArrayList<>();
        LocalDate currentDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        while (!currentDate.isAfter(endLocalDate)) {
            Integer count = usageMap.getOrDefault(currentDate, 0);
            dailyUsage.add(new CouponAnalysisResponseDTO.DailyUsageDTO(
                    currentDate.atStartOfDay(),
                    count
            ));
            currentDate = currentDate.plusDays(1);
        }

        log.info("Generated {} daily usage records", dailyUsage.size());
        return dailyUsage;
    }

    private List<CouponAnalysisResponseDTO.DailyCumulativeDTO> calculateDailyCumulativeFromDB(Long templateId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating daily cumulative for template {} from {} to {}", templateId, startDate, endDate);

        // 🔹 30일 전까지의 누적량 한번에 조회
        Long baseIssuedCount = couponRepository.countIssuedBeforeDate(templateId, startDate);
        Long baseUsedCount = couponRepository.countUsedBeforeDate(templateId, startDate);

        // 30일간 일별 데이터 조회
        List<Object[]> issueResults = couponRepository.findDailyIssueStats(templateId, startDate, endDate);
        List<Object[]> usageResults = couponRepository.findDailyUsageStats(templateId, startDate, endDate);

        log.info("Base counts before 30 days: issued={}, used={}", baseIssuedCount, baseUsedCount);
        log.info("Found {} daily issue records and {} daily usage records", issueResults.size(), usageResults.size());

        // 날짜별 발급/사용 맵 생성
        Map<LocalDate, Integer> dailyIssueMap = new HashMap<>();
        Map<LocalDate, Integer> dailyUsageMap = new HashMap<>();

        for (Object[] result : issueResults) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            Integer count = ((Number) result[1]).intValue();
            dailyIssueMap.put(date, count);
        }

        for (Object[] result : usageResults) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            Integer count = ((Number) result[1]).intValue();
            dailyUsageMap.put(date, count);
        }

        // 🔹 30일간 누적 데이터 생성 (기준값에서 시작해서 매일 더하기)
        List<CouponAnalysisResponseDTO.DailyCumulativeDTO> cumulativeData = new ArrayList<>();
        long cumulativeIssued = baseIssuedCount != null ? baseIssuedCount : 0L;
        long cumulativeUsed = baseUsedCount != null ? baseUsedCount : 0L;

        LocalDate currentDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        while (!currentDate.isAfter(endLocalDate)) {
            // 해당 날짜의 발급/사용 수량 추가
            cumulativeIssued += dailyIssueMap.getOrDefault(currentDate, 0);
            cumulativeUsed += dailyUsageMap.getOrDefault(currentDate, 0);

            cumulativeData.add(new CouponAnalysisResponseDTO.DailyCumulativeDTO(
                    currentDate.atStartOfDay(),
                    (int) cumulativeIssued,
                    (int) cumulativeUsed
            ));

            currentDate = currentDate.plusDays(1);
        }

        log.info("Generated {} cumulative records", cumulativeData.size());
        return cumulativeData;
    }

    private BusinessCouponDTO createExchangingCoupon(ChatRoom room, Long businessId) {
        Long partnerId = room.getRequester().getId().equals(businessId)
                ? room.getRecipient().getId()
                : room.getRequester().getId();


        Business partner = businessRepository.findById(partnerId).orElse(null);
        String partnerName = partner != null ? partner.getBusinessName() : "Unknown";

        return BusinessCouponDTO.builder()
                .id(room.getId())
                .templateId(null)
                .title(partnerName)
                .description(partnerName + "과 1:1 교환으로 작성")
                .maxParticipants(null)
                .progress(0.0)
                .remainingDays(null)
                .status("exchanging")
                .businessName(partnerName)
                .discountValue(0L)
                .owner(room.getRequester().getId().equals(businessId) ? "owner" : "my")
                .currentQuantity(0)
                .totalQuantity(0)
                .roomId(room.getId())
                .chatActive(true)
                .timeAgo(calculateTimeAgo(room.getCreatedAt()))
                .partnerId(partnerId)
                .acceptedByRequester(true)
                .acceptedByRecipient(true)
                .expiredText(null)
                .termsAndConditions("이용약관이 적용됩니다.")
                .build();
    }

    private BusinessCouponDTO convertToBusinessCouponDTO(CouponTemplate template, Long businessId) {
        int usedQuantity = (template.getTotalQuantity() != null && template.getCurrentQuantity() != null)
                ? template.getTotalQuantity() - template.getCurrentQuantity() : 0;

        double progress = 0.0;
        if (template.getTotalQuantity() != null && template.getTotalQuantity() > 0) {
            progress = (double) usedQuantity / template.getTotalQuantity() * 100;
            progress = Math.round(progress * 10.0) / 10.0;
        }

        Integer remainingDays = null;
        if (template.getEndDate() != null) {
            remainingDays = (int) ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    template.getEndDate()
            );
        }

        String status = determineTemplateStatus(template);
        String owner = template.getApplicableBusinessId().equals(businessId) ? "owner" : "my";

        String description = template.getItem();
        if (template.getRoom() != null) {
            Long partnerId = template.getRoom().getRequester().getId().equals(businessId) ?
                    template.getRoom().getRecipient().getId() : template.getRoom().getRequester().getId();

            Business partner = businessRepository.findById(partnerId).orElse(null);
            String partnerName = partner != null ? partner.getBusinessName() : "Unknown";
            description = partnerName + "과 1:1 교환으로 작성";
        }

        Business business = businessRepository.findById(template.getApplicableBusinessId())
                .orElse(null);

        boolean chatActive = template.getRoom() != null &&
                template.getRoom().getStatus() == ChatStatus.ACCEPTED;

        Long roomId = template.getRoom() != null ? template.getRoom().getId() : null;
        Long partnershipId = template.getPartnership() != null ? template.getPartnership().getPartner().getId() : null;

        String title;
        if (template.getItem() != null && template.getDiscountValue() != null) {
            title = template.getItem() + " " + template.getDiscountValue() + "% 할인";
        } else {
            title = template.getDiscountValue() + "% 할인 쿠폰";
        }

        return BusinessCouponDTO.builder()
                .id(template.getId())
                .templateId(template.getId())
                .title(title)
                .description(description)
                .maxParticipants(template.getTotalQuantity())
                .progress(progress)
                .remainingDays(remainingDays)
                .status(status)
                .businessName(business != null ? business.getBusinessName() : "Unknown")
                .discountValue(template.getDiscountValue())
                .owner(owner)
                .currentQuantity(template.getCurrentQuantity())
                .totalQuantity(template.getTotalQuantity())
                .roomId(roomId)
                .chatActive(chatActive)
                .timeAgo(calculateTimeAgo(template.getCreatedAt()))
                .partnerId(partnershipId)
                .acceptedByRequester(true)
                .acceptedByRecipient(chatActive)
                .expiredText(remainingDays != null && remainingDays < 0 ? "만료됨" : null)
                .termsAndConditions("이용약관이 적용됩니다.")
                .build();
    }

    private BusinessCouponDTO convertToBusinessCoupon(CouponTemplate template, Long businessId) {
        int usedQuantity = (template.getTotalQuantity() != null && template.getCurrentQuantity() != null)
                ? template.getTotalQuantity() - template.getCurrentQuantity() : 0;

        double progress = 0.0;
        if (template.getTotalQuantity() != null && template.getTotalQuantity() > 0) {
            progress = (double) usedQuantity / template.getTotalQuantity() * 100;
            progress = Math.round(progress * 10.0) / 10.0;
        }

        Integer remainingDays = null;
        if (template.getEndDate() != null) {
            remainingDays = (int) ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    template.getEndDate()
            );
        }

        String status = determineTemplateStatus(template);
        String owner = template.getApplicableBusinessId().equals(businessId) ? "owner" : "my";

        String description = template.getItem();
        if (template.getRoom() != null) {
            Long partnerId = template.getRoom().getRequester().getId().equals(businessId) ?
                    template.getRoom().getRecipient().getId() : template.getRoom().getRequester().getId();

            Business partner = businessRepository.findById(partnerId).orElse(null);
            String partnerName = partner != null ? partner.getBusinessName() : "Unknown";
            description = partnerName + "에서 제공";
        }

        Business business = businessRepository.findById(template.getApplicableBusinessId())
                .orElse(null);

        boolean chatActive = template.getRoom() != null &&
                template.getRoom().getStatus() == ChatStatus.ACCEPTED;

        Long roomId = template.getRoom() != null ? template.getRoom().getId() : null;
        Long partnershipId = template.getPartnership() != null ? template.getPartnership().getPartner().getId() : null;

        String title;
        if (template.getItem() != null && template.getDiscountValue() != null) {
            title = template.getItem() + " " + template.getDiscountValue() + "% 할인";
        } else {
            title = template.getDiscountValue() + "% 할인 쿠폰";
        }

        return BusinessCouponDTO.builder()
                .id(template.getId())
                .templateId(template.getId())
                .title(title)
                .description(description)
                .maxParticipants(template.getTotalQuantity())
                .progress(progress)
                .remainingDays(remainingDays)
                .status(status)
                .businessName(business != null ? business.getBusinessName() : "Unknown")
                .discountValue(template.getDiscountValue())
                .owner(owner)
                .currentQuantity(template.getCurrentQuantity())
                .totalQuantity(template.getTotalQuantity())
                .roomId(roomId)
                .chatActive(chatActive)
                .timeAgo(calculateTimeAgo(template.getCreatedAt()))
                .partnerId(partnershipId)
                .acceptedByRequester(true)
                .acceptedByRecipient(chatActive)
                .expiredText(remainingDays != null && remainingDays < 0 ? "만료됨" : null)
                .termsAndConditions("이용약관이 적용됩니다.")
                .build();
    }


    private String determineTemplateStatus(CouponTemplate template) {
        LocalDate now = LocalDate.now();

        if (template.getEndDate() != null && template.getEndDate().isBefore(now)) {
            return "expired";
        }

        if (template.getEndDate() != null && !template.getEndDate().isBefore(now)) {
            return "active";
        }

        return "active";
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "Unknown";

        long days = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        if (days > 0) return days + "일 전";

        long hours = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
        if (hours > 0) return hours + "시간 전";

        long minutes = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
        if (minutes > 0) return minutes + "분 전";

        return "방금 전";
    }
}
//public interface BusinessCouponService {
//    BusinessCouponListResponseDTO getBusinessCoupons(Long businessId);
//    CouponAnalysisResponseDTO getCouponAnalysis(Long businessId, Long templateId);
//}

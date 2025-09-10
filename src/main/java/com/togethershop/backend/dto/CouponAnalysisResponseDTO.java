package com.togethershop.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponAnalysisResponseDTO {
    private boolean success;
    private Long templateId;
    private String description;
    private Integer totalIssued;
    private Integer totalUsed;
    private Double usageRate;
    private BigDecimal totalRevenue;
    private List<DailyUsageDTO> dailyUsage;
    private List<DailyCumulativeDTO> dailyCumulative;
    // hourlyDistribution 필드 제거됨
    private Integer currentQuantity;
    private Integer totalQuantity;
    private String termsAndConditions;
    private String message;

    // 새로운 필드 추가
    private BusinessCouponDTO couponDetail; // templateId 기준 상세 정보

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyUsageDTO {
        private LocalDateTime date;
        private Integer count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCumulativeDTO {
        private LocalDateTime date;
        private Integer issued;  // 해당 날짜까지의 누적 발급량
        private Integer used;    // 해당 날짜까지의 누적 사용량
    }
}
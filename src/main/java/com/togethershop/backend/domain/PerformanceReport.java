package com.togethershop.backend.domain;

import com.togethershop.backend.dto.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 20)
    private ReportType reportType;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "coupon_issued_count")
    private Integer couponIssuedCount = 0;

    @Column(name = "coupon_used_count")
    private Integer couponUsedCount = 0;

    @Column(name = "coupon_usage_rate", precision = 5, scale = 2)
    private BigDecimal couponUsageRate = BigDecimal.valueOf(0.00);

    @Column(name = "total_revenue", precision = 12, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.valueOf(0.00);

    @Column(name = "coupon_revenue", precision = 12, scale = 2)
    private BigDecimal couponRevenue = BigDecimal.valueOf(0.00);

    @Column(name = "new_customer_count")
    private Integer newCustomerCount = 0;

    @Column(name = "returning_customer_count")
    private Integer returningCustomerCount = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.valueOf(0.00);

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "partnership_count")
    private Integer partnershipCount = 0;

    @Column(name = "detailed_data", columnDefinition = "json")
    private String detailedData;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

}

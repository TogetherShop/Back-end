package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.dto.CouponStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponCode(String couponCode);
    List<Coupon> findByCustomerIdAndStatus(Long customerId, CouponStatus status);

    @Query("SELECT c FROM Coupon c WHERE c.customerId = :customerId AND c.status = 'ISSUED' " +
            "AND c.expireDate >= CURRENT_TIMESTAMP ORDER BY c.expireDate ASC")
    List<Coupon> findExpiringCoupons(@Param("customerId") Long customerId, @Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);
    List<Coupon> findByExpireDateAndStatus(LocalDate expireDate, CouponStatus status);


    // 특정 템플릿의 총 발급된 쿠폰 개수
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.templateId = :templateId")
    Long countByTemplateId(@Param("templateId") Long templateId);

    // 특정 템플릿의 사용된 쿠폰 개수
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.templateId = :templateId AND c.status = :status")
    Long countByTemplateIdAndStatus(@Param("templateId") Long templateId, @Param("status") CouponStatus status);

    // 특정 날짜 이전까지의 총 발급량 (30일 전까지 누적)
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.templateId = :templateId AND c.issueDate < :beforeDate")
    Long countIssuedBeforeDate(@Param("templateId") Long templateId, @Param("beforeDate") LocalDateTime beforeDate);

    // 특정 날짜 이전까지의 총 사용량 (30일 전까지 누적)
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.templateId = :templateId AND c.status = 'USED' AND c.usedDate < :beforeDate")
    Long countUsedBeforeDate(@Param("templateId") Long templateId, @Param("beforeDate") LocalDateTime beforeDate);

    // 30일간 일별 누적 다운로드량 (발급량) - ISSUED + USED 모든 상태
    @Query("SELECT DATE(c.issueDate) as date, COUNT(c) as dailyCount " +
            "FROM Coupon c " +
            "WHERE c.templateId = :templateId " +
            "AND c.issueDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(c.issueDate) " +
            "ORDER BY DATE(c.issueDate)")
    List<Object[]> findDailyIssueStats(@Param("templateId") Long templateId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // 30일간 일별 누적 사용량 - USED 상태만
    @Query("SELECT DATE(c.usedDate) as date, COUNT(c) as dailyCount " +
            "FROM Coupon c " +
            "WHERE c.templateId = :templateId " +
            "AND c.status = 'USED' " +
            "AND c.usedDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(c.usedDate) " +
            "ORDER BY DATE(c.usedDate)")
    List<Object[]> findDailyUsageStats(@Param("templateId") Long templateId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
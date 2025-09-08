package com.togethershop.backend.repository;

import com.togethershop.backend.domain.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    Optional<PaymentHistory> findTopByCustomerIdOrderByPaymentDateDesc(Long customerId);

    List<PaymentHistory> findTop3ByCustomerIdOrderByPaymentDateDesc(Long customerId);

    // 1. 고객의 가장 최근 방문 매장 조회 (businessId)
    @Query("""
        SELECT p.businessId
        FROM PaymentHistory p
        WHERE p.customerId = :customerId
        ORDER BY p.paymentDate DESC
    """)
    List<Long> findRecentBusinessByCustomerId(@Param("customerId") Long customerId);

    // 2. 최근 방문 매장을 방문한 고객 리스트 조회 (고객 ID)
    @Query("""
        SELECT DISTINCT p.customerId
        FROM PaymentHistory p
        WHERE p.businessId = :businessId
          AND p.customerId <> :customerId
    """)
    List<Long> findOtherCustomersByBusiness(@Param("businessId") Long businessId, @Param("customerId") Long customerId);

    // 3. 2번 리스트 고객들이 최근 방문 매장 방문 이후 방문한 다른 매장 리스트와 카운트 조회
    @Query("""
        SELECT p.businessId, COUNT(p)
        FROM PaymentHistory p
        WHERE p.customerId IN :customerIds
          AND p.paymentDate > :sinceDate
          AND p.businessId <> :businessId
        GROUP BY p.businessId
        ORDER BY COUNT(p) DESC
    """)
    List<Object[]> findRelatedBusinessCount(@Param("customerIds") List<Long> customerIds,
                                            @Param("sinceDate") java.time.LocalDateTime sinceDate,
                                            @Param("businessId") Long businessId);
    @Query("""
    SELECT MAX(p.paymentDate)
    FROM PaymentHistory p
    WHERE p.customerId = :customerId AND p.businessId = :businessId
""")
    LocalDateTime findLatestVisitDate(@Param("customerId") Long customerId, @Param("businessId") Long businessId);

}

package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.RecommendedBusinessDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CustomerBusinessRepository extends JpaRepository<Business, Long> {

    @Query("""
    SELECT new com.togethershop.backend.dto.RecommendedBusinessDTO(
        b.id,
        b.businessName,
        b.description,
        b.address,
        COUNT(p.paymentId),
        COALESCE(AVG(r.rating), 0)
    )
    FROM PaymentHistory p
    JOIN Business b ON p.businessId = b.id
    LEFT JOIN Review r ON b.id = r.businessId AND r.status = 'ACTIVE'
    WHERE p.customerId IN (
        SELECT c.id FROM Customer c
        WHERE c.birth BETWEEN :startDate AND :endDate
    )
    GROUP BY b.id, b.businessName, b.description, b.address
    ORDER BY COUNT(p.paymentId) DESC
    """)
    List<RecommendedBusinessDTO> findRecommendedStoresByBirthDateRange(@Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);

    Iterable<Long> id(Long id);
}

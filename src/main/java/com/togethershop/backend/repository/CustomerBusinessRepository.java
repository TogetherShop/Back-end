package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.BusinessSearchDTO;
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

    @Query("""
        SELECT new com.togethershop.backend.dto.BusinessSearchDTO(
            b.id,
            b.businessName,
            b.businessCategory,
            b.address,
            COALESCE(AVG(r.rating), 0),
            CASE 
                WHEN b.address LIKE 'http%' THEN '온라인'
                ELSE '오프라인'
            END
        )
        FROM Business b
        LEFT JOIN Review r ON r.businessId = b.id AND r.status = 'ACTIVE'
        WHERE LOWER(b.businessName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(b.businessCategory) LIKE LOWER(CONCAT('%', :keyword, '%'))
        GROUP BY b.id, b.businessName, b.businessCategory, b.address
    """)
    List<BusinessSearchDTO> searchByNameOrCategory(@Param("keyword") String keyword);

}

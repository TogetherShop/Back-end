package com.togethershop.backend.repository;

import com.togethershop.backend.domain.CouponTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
    List<CouponTemplate> findByRoomIdIn(List<Long> roomIds);
    List<CouponTemplate> findByPartnershipIn(List<Long> partnershipIds);

    // partnershipId 리스트로 coupon_templates 조회
    List<CouponTemplate> findByPartnership_PartnershipIdIn(List<Long> partnershipIds);

    // 특정 사업자의 applicable_business_id와 일치하는 쿠폰 템플릿 조회 (최신순, 페이징)
    @Query("SELECT ct FROM CouponTemplate ct " +
           "WHERE ct.applicableBusinessId = :businessId " +
           "ORDER BY ct.createdAt DESC")
    List<CouponTemplate> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") Long businessId, PageRequest pageRequest);

    // 특정 사업자의 applicable_business_id와 일치하는 쿠폰 템플릿 조회 (최신순, 전체)
    @Query("SELECT ct FROM CouponTemplate ct " +
           "WHERE ct.applicableBusinessId = :businessId " +
           "ORDER BY ct.createdAt DESC")
    List<CouponTemplate> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") Long businessId);
}

package com.togethershop.backend.repository;

import com.togethershop.backend.domain.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
    List<CouponTemplate> findByRoomIdIn(List<Long> roomIds);
    List<CouponTemplate> findByPartnershipIn(List<Long> partnershipIds);

    // partnershipId 리스트로 coupon_templates 조회
    List<CouponTemplate> findByPartnership_PartnershipIdIn(List<Long> partnershipIds);

    @Query("SELECT ct FROM CouponTemplate ct WHERE ct.room.id = :roomId")
    List<CouponTemplate> findByRoomId(@Param("roomId") Long roomId);

    // room이 null인 경우를 위한 추가 메서드
    List<CouponTemplate> findByApplicableBusinessIdAndRoomIsNull(Long applicableBusinessId);
}
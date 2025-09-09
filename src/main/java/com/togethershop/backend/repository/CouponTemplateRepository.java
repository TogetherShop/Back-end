package com.togethershop.backend.repository;

import com.togethershop.backend.domain.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
    List<CouponTemplate> findByRoomIdIn(List<Long> roomIds);
    List<CouponTemplate> findByPartnershipIn(List<Long> partnershipIds);

    // partnershipId 리스트로 coupon_templates 조회
    List<CouponTemplate> findByPartnership_PartnershipIdIn(List<Long> partnershipIds);


}

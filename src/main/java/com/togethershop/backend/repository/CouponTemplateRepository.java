package com.togethershop.backend.repository;

import com.togethershop.backend.domain.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
    List<CouponTemplate> findByRoomIdInAndIsActive(List<Long> roomIds, Boolean isActive);
}

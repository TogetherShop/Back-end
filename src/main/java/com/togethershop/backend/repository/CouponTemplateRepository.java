package com.togethershop.backend.repository;

import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.domain.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
    List<CouponTemplate> findByRoomRoomIdOrderByCreatedAtDesc(String roomId);

    List<CouponTemplate> findByRoom(ChatRoom room);
}

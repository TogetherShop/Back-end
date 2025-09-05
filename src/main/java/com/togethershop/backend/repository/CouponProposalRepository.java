package com.togethershop.backend.repository;

import com.togethershop.backend.domain.CouponProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponProposalRepository extends JpaRepository<CouponProposal, Long> {
    List<CouponProposal> findByRoomRoomIdOrderByCreatedAtDesc(String roomId);
}

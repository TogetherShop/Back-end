package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Partnership;
import com.togethershop.backend.dto.PartnershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {
    List<Partnership> findByRequester_IdAndStatus(Long requesterId, PartnershipStatus status);
    List<Partnership> findByPartner_IdAndStatus(Long partnerId, PartnershipStatus status);
    // 맞춤설정: requesterBusinessId 필드명이 실제 엔티티에서 다르면 변경필요
    List<Partnership> findByRequester_IdInAndStatus(List<Long> requesterBusinessIds, PartnershipStatus status);

}
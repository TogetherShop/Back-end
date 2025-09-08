package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Partnership;
import com.togethershop.backend.dto.PartnershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {



    // 특정 businessId가 requester 또는 partner인 모든 파트너십 조회
    List<Partnership> findByRequester_IdOrPartner_Id(Long requesterId, Long partnerId);
    // requester(연관관계)를 통해 검색
    List<Partnership> findByRequester_Id(Long requesterId);

    // partner(연관관계)를 통해 검색
    List<Partnership> findByPartner_Id(Long partnerBusinessId);

    // 여러 requester 대상으로 검색
    List<Partnership> findByRequester_IdIn(List<Long> requesterIds);

    // 여러 partner 대상으로 검색
    List<Partnership> findByPartner_IdIn(List<Long> partnerIds);
}
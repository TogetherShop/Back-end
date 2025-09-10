package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {


    // requester(연관관계)를 통해 검색
    List<Partnership> findByRequester_Id(Long requesterId);

    // partner(연관관계)를 통해 검색
    List<Partnership> findByPartner_Id(Long partnerBusinessId);

    // 여러 requester 대상으로 검색
    List<Partnership> findByRequester_IdIn(List<Long> requesterIds);

    // 여러 partner 대상으로 검색
    List<Partnership> findByPartner_IdIn(List<Long> partnerIds);

    // 특정 businessId가 requester 또는 partner인 모든 파트너십 조회
    List<Partnership> findByRequester_IdOrPartner_Id(Long requesterId, Long partnerId);


    // 반대 순서(A-B, B-A)까지 확인
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Partnership p " +
            "WHERE p.requester.id = :userId AND p.partner.id = :otherId")
    boolean existsPartnership(@Param("userId") Long userId, @Param("otherId") Long otherId);


    boolean existsByRequesterIdOrPartnerId(Long requesterId, Long partnerId);

    Optional<Partnership> findByRequesterAndPartner(Business requester, Business partner);

    // requester 기준으로 partnership 찾기
    Optional<Partnership> findByRequester(Business requester);

    // recipient 기준으로도 필요하면 추가
    Optional<Partnership> findByPartner(Business partner);

}
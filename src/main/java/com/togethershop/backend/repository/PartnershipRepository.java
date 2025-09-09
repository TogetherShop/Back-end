package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {

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
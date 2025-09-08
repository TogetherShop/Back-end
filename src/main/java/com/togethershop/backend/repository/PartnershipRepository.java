package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {
    Optional<Partnership> findByRequester_IdOrPartner_Id(Long businessId, Long businessId2);

    // 정확히 같은 순서
    boolean existsByRequester_IdAndPartner_Id(Long requesterId, Long partnerId);

    // 반대 순서(A-B, B-A)까지 확인
    default boolean existsPartnership(Long businessId1, Long businessId2) {
        return existsByRequester_IdAndPartner_Id(businessId1, businessId2) ||
                existsByRequester_IdAndPartner_Id(businessId2, businessId1);
    }
}

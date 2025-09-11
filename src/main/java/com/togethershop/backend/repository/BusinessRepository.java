package com.togethershop.backend.repository;


import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findFirstByUsernameOrderByIdAsc(String username);

    // 메서드 이름 기준 Between 사용 버전
    List<Business> findByStatusAndLatitudeIsNotNullAndLongitudeIsNotNullAndLatitudeBetweenAndLongitudeBetween(
            AccountStatus status, Double swLat, Double neLat, Double swLng, Double neLng
    );
}

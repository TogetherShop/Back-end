package com.togethershop.backend.repository;


import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {
//    // 바운딩박스(지도의 남서-북동 모서리) 안에 있는 활성 매장만 가져오기
//    List<Business> findByStatusAndLatitudeIsNotNullAndLongitudeIsNotNull
//    (AccountStatus status,
//     Double swLat, Double neLat,
//     Double swLng, Double neLng);

    // 메서드 이름 기준 Between 사용 버전
    List<Business> findByStatusAndLatitudeIsNotNullAndLongitudeIsNotNullAndLatitudeBetweenAndLongitudeBetween(
            AccountStatus status, Double swLat, Double neLat, Double swLng, Double neLng
    );
}

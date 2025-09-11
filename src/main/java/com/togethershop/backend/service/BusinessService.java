package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.AccountStatus;
import com.togethershop.backend.dto.BusinessDTO;
import com.togethershop.backend.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;

    /** 초기 로딩: 중심 좌표 + 반경(m)으로 주변 매장 검색 (JPA만 사용) */
    public List<BusinessDTO> findNearby(double centerLat, double centerLng, int radiusMeters, int limit) {
        // 1) 반경 → 대략적 바운딩박스 계산 (쿼리 범위 축소)
        double latDelta = metersToDegreesLat(radiusMeters);
        double lngDelta = metersToDegreesLng(radiusMeters, centerLat);

        double swLat = centerLat - latDelta;
        double neLat = centerLat + latDelta;
        double swLng = centerLng - lngDelta;
        double neLng = centerLng + lngDelta;

        // 2) 박스 내 ACTIVE 매장 조회 (DB 레벨 필터)
        List<Business> raw = businessRepository
                .findByStatusAndLatitudeIsNotNullAndLongitudeIsNotNullAndLatitudeBetweenAndLongitudeBetween(
                        AccountStatus.ACTIVE, swLat, neLat, swLng, neLng);

        // 3) 자바에서 거리 정밀 계산 → 반경 내 필터 → 거리순 정렬 → 제한
        return raw.stream()
                .map(b -> toDto(b, centerLat, centerLng))
                .filter(d -> d.getDistance() != null && d.getDistance() <= radiusMeters)
                .sorted(Comparator.comparingInt(BusinessDTO::getDistance))
                .limit(limit)
                .toList();
    }

    /** 지도 가시영역(남서/북동) 내 매장 조회 → 중심까지의 거리로 정렬 */
    public List<BusinessDTO> findInBounds(
            double swLat, double swLng, double neLat, double neLng,
            double centerLat, double centerLng, int limit) {

        List<Business> raw = businessRepository
                .findByStatusAndLatitudeIsNotNullAndLongitudeIsNotNullAndLatitudeBetweenAndLongitudeBetween(
                        AccountStatus.ACTIVE, swLat, neLat, swLng, neLng);

        return raw.stream()
                .map(b -> toDto(b, centerLat, centerLng))
                .sorted(Comparator.comparingInt(BusinessDTO::getDistance))
                .limit(limit)
                .toList();
    }

    /** 엔티티 → DTO 매핑 (+거리/도보시간 계산) */
    private BusinessDTO toDto(Business b, double centerLat, double centerLng) {
        Double lat = b.getLatitude();
        Double lng = b.getLongitude();
        Integer dist = null, walk = null;

        if (lat != null && lng != null) {
            int d = (int)Math.round(haversineMeters(centerLat, centerLng, lat, lng));
            dist = d;
            walk = Math.max(1, (int)Math.round(d / 80.0)); // 80m/분 가정
        }

        return BusinessDTO.builder()
                .businessId(b.getId())
                .businessName(b.getBusinessName())
                .type(b.getBusinessType())
                .businessCategory(b.getBusinessCategory())
                .address(b.getAddress())
                .lat(lat)
                .lng(lng)
                .distance(dist)
                .walkTime(walk)
                .build();
    }

    /** 하버사인 거리(m) */
    private static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371_000.0; // m
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
                        Math.sin(dLng/2)*Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private static double metersToDegreesLat(double meters) {
        return meters / 111_320.0; // 위도 1도 ≈ 111.32km
    }

    private static double metersToDegreesLng(double meters, double atLat) {
        double metersPerDeg = 111_320.0 * Math.cos(Math.toRadians(atLat));
        if (metersPerDeg < 1e-6) metersPerDeg = 1e-6; // 극지 보호
        return meters / metersPerDeg;
    }
}

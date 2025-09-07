package com.togethershop.backend.service;


import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.dto.CustomerVisitPatternResponseDTO;
import com.togethershop.backend.dto.RecommendedBusinessDTO;
import com.togethershop.backend.dto.RelatedBusinessDTO;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.CustomerBusinessRepository;
import com.togethershop.backend.repository.CustomerRepository;
import com.togethershop.backend.repository.PaymentHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerRecommendationService {
    private final CustomerRepository customerRepository;
    private final CustomerBusinessRepository businessRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;


    public List<RecommendedBusinessDTO> getRecommendedStores(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID"));

        // 연령대 대신, 생일 범위를 계산한다.
        LocalDate now = LocalDate.now();

        AgeRange ageRange = getAgeRange(calculateAge(customer.getBirth()));

        // 생년월일 범위 계산 (startDate, endDate)
        LocalDate startDate = now.minusYears(ageRange.getMax() + 1).plusDays(1);
        LocalDate endDate = now.minusYears(ageRange.getMin());

        return businessRepository.findRecommendedStoresByBirthDateRange(startDate, endDate);
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private AgeRange getAgeRange(int age) {
        if (age < 20) return new AgeRange(0, 19);
        else if (age < 30) return new AgeRange(20, 29);
        else if (age < 40) return new AgeRange(30, 39);
        else if (age < 50) return new AgeRange(40, 49);
        else if (age < 60) return new AgeRange(50, 59);
        else return new AgeRange(60, 150);
    }

    @Getter
    @AllArgsConstructor
    private static class AgeRange {
        private final int min;
        private final int max;
    }

    public CustomerVisitPatternResponseDTO getVisitPattern(Long customerId) {
        // 1. 고객의 가장 최근 방문 매장 조회
        List<Long> recentBusinesses = paymentHistoryRepository.findRecentBusinessByCustomerId(customerId);
        if (recentBusinesses.isEmpty()) {
            log.info("recentBusinesses.isEmpty()");
            return CustomerVisitPatternResponseDTO.builder()
                    .recentBusiness(null)
                    .relatedBusiness(Collections.emptyList())
                    .build();
        }

        Long recentBusinessId = recentBusinesses.get(0);

        Business recentBusiness = businessRepository.findById(recentBusinessId).orElse(null);

        RelatedBusinessDTO recentBusinessDTO = recentBusiness != null ? RelatedBusinessDTO.builder()
                .businessId(recentBusiness.getId())
                .businessName(recentBusiness.getBusinessName())
                .businessCategory(recentBusiness.getBusinessCategory())
                .associationRate(null)  // 최근 방문 매장에는 연관율 없음
                .build() : null;

        // 2. 해당 매장 방문한 다른 고객 리스트
        List<Long> otherCustomers = paymentHistoryRepository.findOtherCustomersByBusiness(recentBusinessId, customerId);
        if (otherCustomers.isEmpty()) {
            log.info("otherCustomers.isEmpty()");
            return CustomerVisitPatternResponseDTO.builder()
                    .recentBusiness(recentBusinessDTO)
                    .relatedBusiness(Collections.emptyList())
                    .build();
        }

        // 3. 가장 최근 방문일자 조회 (본인)
        LocalDateTime recentVisitDate = paymentHistoryRepository.findLatestVisitDate(customerId, recentBusinessId);
        if (recentVisitDate == null) {
            log.info("recentVisitDate.isEmpty()");
            return CustomerVisitPatternResponseDTO.builder()
                    .recentBusiness(recentBusinessDTO)
                    .relatedBusiness(Collections.emptyList())
                    .build();
        }

        // 4. 다른 고객들이 최근 방문 매장 이후 방문한 다른 매장 조회 + 방문 횟수
        List<Object[]> relatedBusinessCounts = paymentHistoryRepository.findRelatedBusinessCount(otherCustomers, recentVisitDate, recentBusinessId);
        if (relatedBusinessCounts.isEmpty()) {
            log.info("relatedBusinessCounts.isEmpty()");
            return CustomerVisitPatternResponseDTO.builder()
                    .recentBusiness(recentBusinessDTO)
                    .relatedBusiness(Collections.emptyList())
                    .build();
        }

        long totalVisits = relatedBusinessCounts.stream()
                .mapToLong(arr -> (Long) arr[1])
                .sum();

        List<Long> businessIds = relatedBusinessCounts.stream()
                .map(arr -> (Long) arr[0])
                .collect(Collectors.toList());

        Map<Long, Business> businessMap = businessRepository.findAllById(businessIds).stream()
                .collect(Collectors.toMap(Business::getId, b -> b));

        // 5. DTO 변환 + 연관 방문률 계산
        List<RelatedBusinessDTO> relatedStores = relatedBusinessCounts.stream()
                .map(arr -> {
                    Long bId = (Long) arr[0];
                    Long count = (Long) arr[1];
                    Business b = businessMap.get(bId);
                    double rate = totalVisits > 0 ? (double) count / totalVisits : 0;
                    return RelatedBusinessDTO.builder()
                            .businessId(bId)
                            .businessName(b != null ? b.getBusinessName() : null)
                            .businessCategory(b != null ? b.getBusinessCategory() : null)
                            .associationRate(rate)
                            .build();
                }).collect(Collectors.toList());

        return CustomerVisitPatternResponseDTO.builder()
                .recentBusiness(recentBusinessDTO)
                .relatedBusiness(relatedStores)
                .build();
    }



}

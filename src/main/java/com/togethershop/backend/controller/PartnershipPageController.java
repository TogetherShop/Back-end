package com.togethershop.backend.controller;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.PartnershipDetailDTO;
import com.togethershop.backend.dto.PartnershipListDTO;
import com.togethershop.backend.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partnership-page")
@RequiredArgsConstructor
@Slf4j
public class PartnershipPageController {
    
    private final BusinessRepository businessRepository;

    /**
     * 제휴페이지 매장 목록 조회 API
     * 프론트에서 제휴 매장 리스트를 연동할 때 사용
     */
    @GetMapping("/businesses")
    public ResponseEntity<List<PartnershipListDTO>> getPartnershipBusinesses() {
        List<Business> businesses = businessRepository.findAll();

        List<PartnershipListDTO> partnershipList = businesses.stream()
            .map(business -> PartnershipListDTO.builder()
                .businessId(business.getId())
                .businessName(business.getBusinessName())
                .businessCategory(business.getBusinessCategory())
                .address(business.getAddress())
                .latitude(business.getLatitude())
                .longitude(business.getLongitude())
                .togetherIndex(business.getTogetherIndex())
                .profileImageUrl(business.getProfileImageUrl())
                .description(business.getDescription())
                .collaborationCategory(business.getCollaborationCategory())
                .build())
            .toList();

        return ResponseEntity.ok(partnershipList);
    }

    /**
     * 제휴페이지 매장 상세 조회 API
     * 매장 상세 정보를 조회할 때 사용
     */
    @GetMapping("/businesses/{businessId}")
    public ResponseEntity<PartnershipDetailDTO> getPartnershipBusinessDetail(@PathVariable Long businessId) {
        Business business = businessRepository.findById(businessId)
            .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + businessId));

        PartnershipDetailDTO detailDTO = PartnershipDetailDTO.builder()
            .businessId(business.getId())
            .businessName(business.getBusinessName())
            .businessCategory(business.getBusinessCategory())
            .address(business.getAddress())
            .latitude(business.getLatitude())
            .longitude(business.getLongitude())
            .businessHours(business.getBusinessHours())
            .togetherIndex(business.getTogetherIndex())
            .profileImageUrl(business.getProfileImageUrl())
            .description(business.getDescription())
            .collaborationCategory(business.getCollaborationCategory())
            .phoneNumber(business.getPhoneNumber())
            .businessType(business.getBusinessType())
            .build();

        return ResponseEntity.ok(detailDTO);
    }
}

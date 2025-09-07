package com.togethershop.backend.controller;


import com.togethershop.backend.dto.CustomerVisitPatternResponseDTO;
import com.togethershop.backend.dto.RecommendedBusinessDTO;
import com.togethershop.backend.dto.RelatedBusinessDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.CustomerRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class CustomerRecommendationController {
    private final CustomerRecommendationService customerRecommendationService;

    @GetMapping("/recommended")
    public ResponseEntity<List<RecommendedBusinessDTO>> getRecommendedStores(@AuthenticationPrincipal CustomUserDetails user) {
        Long customerId = user.getUserId();
        List<RecommendedBusinessDTO> stores = customerRecommendationService.getRecommendedStores(customerId);
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/visit-pattern")
    public ResponseEntity<CustomerVisitPatternResponseDTO> getVisitPattern(@AuthenticationPrincipal CustomUserDetails user) {
        Long customerId = user.getUserId();
        CustomerVisitPatternResponseDTO response = customerRecommendationService.getVisitPattern(customerId);
        return ResponseEntity.ok(response);
    }
}

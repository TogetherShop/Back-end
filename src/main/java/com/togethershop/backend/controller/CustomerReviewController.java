package com.togethershop.backend.controller;

import com.togethershop.backend.dto.ReviewResponseDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.CustomerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerReviewController {

    private final CustomerReviewService customerReviewService;

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getRecentReviews(@AuthenticationPrincipal CustomUserDetails user) {
        Long customerId = user.getUserId();
        List<ReviewResponseDTO> reviews = customerReviewService.getRecentReviews(customerId);
        return ResponseEntity.ok(reviews);
    }
}

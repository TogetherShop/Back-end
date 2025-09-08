package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.Review;
import com.togethershop.backend.dto.ReviewResponseDTO;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerReviewService {
    private final ReviewRepository reviewRepository;
    private final BusinessRepository businessRepository;

    public List<ReviewResponseDTO> getRecentReviews(Long customerId) {
        List<Review> reviews = reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        if (reviews.isEmpty()) {
            return List.of();
        }

        List<Long> businessIds = reviews.stream()
                .map(Review::getBusinessId)
                .distinct()
                .collect(Collectors.toList());

        // business 조회 후 businessName과 address 함께 매핑
        var businessMap = businessRepository.findAllById(businessIds).stream()
                .collect(Collectors.toMap(
                        Business::getId,
                        b -> new BusinessInfo(b.getBusinessName(), b.getAddress())
                ));

        return reviews.stream()
                .map(review -> {
                    BusinessInfo info = businessMap.get(review.getBusinessId());
                    return ReviewResponseDTO.builder()
                            .reviewId(review.getId())
                            .businessId(review.getBusinessId())
                            .businessName(info != null ? info.getBusinessName() : null)
                            .address(info != null ? info.getAddress() : null)
                            .content(review.getContent())
                            .rating(review.getRating())
                            .createdAt(review.getCreatedAt())
                            .status(review.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // DTO 내부나 별도 static class로 business 이름+주소 묶음 추가
    @Data
    @AllArgsConstructor
    public static class BusinessInfo {
        private String businessName;
        private String address;
    }

}

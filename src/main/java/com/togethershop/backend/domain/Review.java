package com.togethershop.backend.domain;

import com.togethershop.backend.dto.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;  // 작성 고객

    @Column(name = "business_id", nullable = false)
    private Long businessId;  // 대상 사업자

    @Column(nullable = false)
    private Double rating;    // 평점 (0-5)

    @Column(columnDefinition = "TEXT")
    private String content;   // 리뷰 내용

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReviewStatus status = ReviewStatus.ACTIVE;

}

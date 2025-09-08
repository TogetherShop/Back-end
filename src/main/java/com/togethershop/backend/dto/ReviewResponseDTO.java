package com.togethershop.backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long reviewId;
    private Long businessId;
    private String address;           // 주소 추가
    private String businessName;       // 비즈니스 이름 포함
    private String content;
    private Double rating;              // 평점 0~5
    private LocalDateTime createdAt;
    private String status;             // ACTIVE, HIDDEN, DELETED 등
}
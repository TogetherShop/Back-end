package com.togethershop.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPurchaseProjectDTO {
    // Entity 기본 필드들 (DDL 기준)
    private Long id; // project_id
    private Long businessId; // business_id
    private String businessName; // Business 테이블에서 조인
    private String description; // description (실제 제목 역할)
    private Integer totalQuantity; // total_quantity
    private Integer targetNumber; // target_number (목표 인원)
    private Long targetMoney; // target_money
    private String accountNumber; // account_number
    private String accountHost; // account_host
    private String status; // status (OPEN, CLOSED, FULFILLED, CANCELLED)
    private LocalDateTime endDate; // end_date
    private LocalDateTime createdAt; // created_at
    
    // 계산된 필드들
    private Integer currentQuantity; // 현재 참여 인원 (계산값)
    private Integer participantCount; // 참여자 수 (계산값)
    private Integer remainingQuantity; // 남은 인원 (계산값)
    private Double progressPercentage; // 진행률 (계산값)
    private Long daysRemaining; // 남은 일수 (계산값)
}

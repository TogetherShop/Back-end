package com.togethershop.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPurchaseParticipantDTO {
    // Entity 기본 필드들 (DDL 기준)
    private Long id; // participant_id
    private Long projectId; // project_id
    private Long businessId; // business_id
    private String businessName; // Business 테이블에서 조인
    private String businessCategory; // Business 테이블에서 조인
    private GroupPurchaseStatus status; // status (APPLIED, CONFIRMED, CANCELLED)
    private LocalDateTime joinedAt; // joined_at
}

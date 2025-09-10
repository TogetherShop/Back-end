package com.togethershop.backend.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPurchaseParticipationRequestDTO {
    
    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;
}

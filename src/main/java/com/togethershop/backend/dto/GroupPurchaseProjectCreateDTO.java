package com.togethershop.backend.dto;

import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPurchaseProjectCreateDTO {
    
    @NotBlank(message = "프로젝트 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;
    
    @NotBlank(message = "프로젝트 설명은 필수입니다")
    private String description;
    
    @NotNull(message = "목표 수량은 필수입니다")
    @Min(value = 1, message = "목표 수량은 1개 이상이어야 합니다")
    private Integer targetQuantity;
    
    @NotNull(message = "목표 금액은 필수입니다")
    @Min(value = 1000, message = "목표 금액은 1,000원 이상이어야 합니다")
    private Long targetMoney;
    
    @NotBlank(message = "계좌번호는 필수입니다")
    @Pattern(regexp = "^[0-9\\-]+$", message = "올바른 계좌번호 형식이 아닙니다")
    private String accountNumber;
    
    @NotBlank(message = "계좌주명은 필수입니다")
    @Size(max = 50, message = "계좌주명은 50자를 초과할 수 없습니다")
    private String accountHost;
    
    @NotNull(message = "종료일은 필수입니다")
    @Future(message = "종료일은 현재 시간보다 나중이어야 합니다")
    private LocalDateTime endDate;
}

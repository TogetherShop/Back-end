package com.togethershop.backend.dto;

import com.togethershop.backend.domain.Business;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessDTO {
    private Long businessId;
    private String businessName;
    private String businessCategory;
    private String phoneNumber;
    private String address;
    // 필요에 따라 필드 추가


}

package com.togethershop.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDTO {
    private Long id;
    private String name;
    private PartnerStatus status;
    private String statusLabel;
    private String detail;
}

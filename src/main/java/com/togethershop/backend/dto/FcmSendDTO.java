package com.togethershop.backend.dto;


import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmSendDTO {
    private String token;

    private String title;

    private String body;

    private String image; // Optional

}

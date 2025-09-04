package com.togethershop.backend.dto;

import lombok.Data;

@Data
public class SignupRequestDTO {
    private String username;
    private String password;
    private String shopName;
}

package com.togethershop.backend.dto;

import lombok.Data;

@Data
public class SignupRequestDTO {
    private String username;             // 로그인 아이디
    private String email;                // 이메일
    private String password;             // 비밀번호
    private String businessName;             // 상호명
    private String businessRegistrationNumber; // 사업자등록번호
    private String businessType;         // 업종
    private String businessCategory;     // 주요 고객층
    private String collaborationCategory; // 협업 희망 업종
}

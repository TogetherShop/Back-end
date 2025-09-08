package com.togethershop.backend.controller;

import com.togethershop.backend.dto.CustomerProfileDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@AuthenticationPrincipal CustomUserDetails user) {
        Long customerId = user.getUserId(); // CustomUserDetails 에 userId getter 있다고 가정
        CustomerProfileDTO profile = customerProfileService.getCustomerProfile(customerId);
        return ResponseEntity.ok(profile);
    }
}

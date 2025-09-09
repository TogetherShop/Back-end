package com.togethershop.backend.controller;


import com.togethershop.backend.dto.FcmSendDTO;
import com.togethershop.backend.dto.FcmTokenRequestDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/customer/fcm-token")
    public ResponseEntity<Void> updateCustomerFcmToken(@RequestBody FcmTokenRequestDTO request, @AuthenticationPrincipal CustomUserDetails user) {
        fcmService.updateCustomerFcmToken(user.getUserId(), request.getFcmToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/business/fcm-token")
    public ResponseEntity<Void> updateBusinessFcmToken(@RequestBody FcmTokenRequestDTO request, @AuthenticationPrincipal CustomUserDetails user) {
        fcmService.updateBusinessFcmToken(user.getUserId(), request.getFcmToken());
        return ResponseEntity.ok().build();
    }

}

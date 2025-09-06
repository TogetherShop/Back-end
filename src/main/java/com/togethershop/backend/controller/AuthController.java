package com.togethershop.backend.controller;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.LoginRequestDTO;
import com.togethershop.backend.dto.SignupRequestDTO;
import com.togethershop.backend.repository.ShopUserRepository;
import com.togethershop.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ShopUserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDTO req) {
        try {
            // DTO 전체를 서비스에 전달
            Business u = authService.signup(req);
            return ResponseEntity.ok(Map.of(
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "email", u.getEmail(),
                    "businessName", u.getBusinessName(),
                    "businessRegistrationNumber", u.getBusinessRegistrationNumber(),
                    "businessType", u.getBusinessType(),
                    "businessCategory", u.getBusinessCategory(),
                    "collaborationCategory", u.getCollaborationCategory(),
                    "status", u.getStatus(),
                    "verificationStatus", u.getVerificationStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO req) {
        try {
            Map<String, Object> tokens = authService.login(req.getUsername(), req.getPassword());
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refresh = body.get("refreshToken");
        try {
            Map<String, Object> tokens = authService.refresh(refresh);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String refresh = body.get("refreshToken");
        if (refresh != null) authService.logoutByRefreshToken(refresh);
        return ResponseEntity.ok(Map.of("result", "ok"));
    }

}


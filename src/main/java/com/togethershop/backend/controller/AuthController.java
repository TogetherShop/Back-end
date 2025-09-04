package com.togethershop.backend.controller;

import com.togethershop.backend.domain.ShopUser;
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
            ShopUser u = authService.signup(req.getUsername(), req.getPassword(), req.getShopName());
            return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername()));
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


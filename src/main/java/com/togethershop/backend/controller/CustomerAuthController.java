package com.togethershop.backend.controller;

import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.dto.CustomerLoginRequestDTO;
import com.togethershop.backend.dto.CustomerSignupRequestDTO;
import com.togethershop.backend.repository.CustomerRepository;
import com.togethershop.backend.service.CustomerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthService authService;
    private final CustomerRepository customerRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody CustomerSignupRequestDTO dto) {
        try {
            Customer customer = authService.signup(dto);
            return ResponseEntity.ok(Map.of(
                    "id", customer.getId(),
                    "username", customer.getUsername(),
                    "email", customer.getEmail(),
                    "name", customer.getName(),
                    "birth", customer.getBirth(),
                    "status", customer.getStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CustomerLoginRequestDTO dto) {
        try {
            Map<String, Object> tokens = authService.login(dto.getUsername(), dto.getPassword());
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

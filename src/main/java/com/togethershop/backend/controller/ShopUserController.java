package com.togethershop.backend.controller;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.ShopUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ShopUserController {

    private final ShopUserService userService;

    /**
     * 전체 회원 조회
     */
    @GetMapping
    public List<Business> getAllUsers(@RequestParam(value = "q", required = false) String query,
                                      @AuthenticationPrincipal String currentUsername) {
        if (query == null || query.isBlank()) {
            return userService.findAllExcept(currentUsername);
        }
        return userService.searchByUsername(query, currentUsername);
    }

    /**
     * 특정 회원 조회
     */
    @GetMapping("/{id}")
    public Business getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /**
     * 내 비즈니스 정보 조회 (JWT 토큰 기반)
     */
    @GetMapping("/me")
    public ResponseEntity<Business> getMyBusinessInfo(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long businessId = userDetails.getUserId();
        
        Business business = userService.findById(businessId);
        return ResponseEntity.ok(business);
    }
}

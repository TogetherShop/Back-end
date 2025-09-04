package com.togethershop.backend.controller;

import com.togethershop.backend.domain.ShopUser;
import com.togethershop.backend.service.ShopUserService;
import lombok.RequiredArgsConstructor;
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
    public List<ShopUser> getAllUsers(@RequestParam(value = "q", required = false) String query,
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
    public ShopUser getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
}

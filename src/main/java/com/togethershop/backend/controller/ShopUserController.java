package com.togethershop.backend.controller;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.dto.PartnershipDTO;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.PartnershipService;
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
    private final PartnershipService partnershipService;

    /**
     * 전체 회원 조회
     */
    @GetMapping
    public List<PartnershipDTO> getAllUsers(@RequestParam(value = "q", required = false) String query,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Long currentUserId = currentUser.getUserId();

        // 전체 사용자 + partnershipExists 정보 같이 내려줌
        List<PartnershipDTO> allUsers = partnershipService.getAllBusinesses(currentUserId);

        // 검색어가 있을 경우 필터링
        if (query != null && !query.isBlank()) {
            String qLower = query.toLowerCase();
            allUsers = allUsers.stream()
                    .filter(u -> u.getName().toLowerCase().contains(qLower)
                            || u.getCategory().toLowerCase().contains(qLower))
                    .toList();
        }

        return allUsers;
    }

    /**
     * 특정 회원 조회
     */
    @GetMapping("/{id}")
    public Business getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
}

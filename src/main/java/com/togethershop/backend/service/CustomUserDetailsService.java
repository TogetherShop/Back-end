package com.togethershop.backend.service;


import com.togethershop.backend.domain.Business;
import com.togethershop.backend.repository.ShopUserRepository;
import com.togethershop.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ShopUserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Business user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));

        return CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .authorities(Collections.emptyList())
                .build();
    }

    // JWT 토큰에서 userId 기반으로 직접 로딩하고 싶다면 아래 메서드도 추가
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        Business user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .authorities(Collections.emptyList())
                .build();
    }
}


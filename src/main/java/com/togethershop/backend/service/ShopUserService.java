package com.togethershop.backend.service;

import com.togethershop.backend.domain.ShopUser;
import com.togethershop.backend.repository.ShopUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopUserService {

    private final ShopUserRepository userRepository;

    /**
     * 전체 사용자 조회 (로그인한 사용자 제외 가능)
     */
    public List<ShopUser> findAllExcept(String username) {
        if (username == null) {
            return userRepository.findAll();
        }
        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(username))
                .toList();
    }

    /**
     * 사용자 검색 (username 기준, 일부 문자열 검색)
     */
    public List<ShopUser> searchByUsername(String query, String currentUsername) {
        String lowerQuery = query.toLowerCase();
        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(currentUsername))
                .filter(u -> u.getUsername().toLowerCase().contains(lowerQuery))
                .toList();
    }

    /**
     * ID로 사용자 조회
     */
    public ShopUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}

package com.togethershop.backend.service;

import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.domain.CustomerRefreshToken;
import com.togethershop.backend.dto.AccountStatus;
import com.togethershop.backend.dto.CustomerSignupRequestDTO;
import com.togethershop.backend.repository.CustomerRefreshTokenRepository;
import com.togethershop.backend.repository.CustomerRepository;
import com.togethershop.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerAuthService {

    private final CustomerRepository customerRepository;
    private final CustomerRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Customer signup(CustomerSignupRequestDTO dto) {
        if (customerRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        Customer customer = Customer.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .birth(dto.getBirth())
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return customerRepository.save(customer);
    }

    @Transactional
    public Map<String, Object> login(String username, String rawPassword) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (!passwordEncoder.matches(rawPassword, customer.getPasswordHash())) {
            throw new BadCredentialsException("비밀번호 불일치");
        }

        refreshTokenRepository.deleteByCustomer(customer);

        String accessToken = jwtTokenProvider.createAccessToken(customer.getId(), "CUSTOMER");
        String refreshTokenValue = jwtTokenProvider.createRefreshToken();
        Date refreshExp = jwtTokenProvider.getClaims(refreshTokenValue).getExpiration();

        CustomerRefreshToken customerRefreshToken = CustomerRefreshToken.builder()
                .token(refreshTokenValue)
                .customer(customer)
                .expiryDate(refreshExp)
                .revoked(false)
                .build();

        refreshTokenRepository.save(customerRefreshToken);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshTokenValue);
        result.put("accessTokenExpiresIn",
                Instant.now().plusMillis(jwtTokenProvider.getClaims(accessToken).getExpiration().getTime() - new Date().getTime())
        );
        return result;
    }

    @Transactional
    public Map<String, Object> refresh(String providedRefreshToken) {
        CustomerRefreshToken stored = refreshTokenRepository.findByToken(providedRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰"));

        if (stored.isRevoked()) throw new IllegalArgumentException("리프레시 토큰이 취소됨");
        if (stored.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(stored);
            throw new IllegalArgumentException("리프레시 토큰 만료");
        }

        Customer customer = stored.getCustomer();

        refreshTokenRepository.delete(stored);

        String newAccessToken = jwtTokenProvider.createAccessToken(customer.getId(), "CUSTOMER");
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken();
        Date newRefreshExp = jwtTokenProvider.getClaims(newRefreshTokenValue).getExpiration();

        CustomerRefreshToken newRefresh = CustomerRefreshToken.builder()
                .token(newRefreshTokenValue)
                .customer(customer)
                .expiryDate(newRefreshExp)
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefresh);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshTokenValue);
    }

    @Transactional
    public void logoutByRefreshToken(String providedRefreshToken) {
        refreshTokenRepository.findByToken(providedRefreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Transactional
    public void logoutByCustomer(Customer customer) {
        refreshTokenRepository.deleteByCustomer(customer);
    }
}

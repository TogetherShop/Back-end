package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.BusinessRefreshToken;
import com.togethershop.backend.dto.AccountStatus;
import com.togethershop.backend.dto.SignupRequestDTO;
import com.togethershop.backend.dto.VerificationStatus;
import com.togethershop.backend.repository.RefreshTokenRepository;
import com.togethershop.backend.repository.ShopUserRepository;
import com.togethershop.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class AuthService {

    private final ShopUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Business signup(SignupRequestDTO req) {
        // 아이디 중복 체크
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        Business user = Business.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword())) // password_hash에 저장
                .businessName(req.getBusinessName())
                .businessRegistrationNumber(req.getBusinessRegistrationNumber())
                .businessType(req.getBusinessType())
                .businessCategory(req.getBusinessCategory())
                .collaborationCategory(req.getCollaborationCategory())
                .verificationStatus(VerificationStatus.PENDING)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }


    @Transactional
    public Map<String, Object> login(String username, String rawPassword) {
        Business user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("비밀번호 불일치");
        }

        refreshTokenRepository.deleteByUser(user);
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), "USER");
        String refreshTokenValue = jwtTokenProvider.createRefreshToken();

        // refresh token 만료시간을 JWT claim에서 읽음
        Date refreshExp = jwtTokenProvider.getClaims(refreshTokenValue).getExpiration();


        BusinessRefreshToken businessRefreshToken = BusinessRefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(refreshExp)
                .revoked(false)
                .build();
        refreshTokenRepository.save(businessRefreshToken);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshTokenValue);
        result.put("accessTokenExpiresIn", Instant.now().plusMillis(jwtTokenProvider.getClaims(accessToken).getExpiration().getTime() - new Date().getTime()));
        return result;
    }

    @Transactional
    public Map<String, Object> refresh(String providedRefreshToken) {
        BusinessRefreshToken stored = refreshTokenRepository.findByToken(providedRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰"));

        if (stored.isRevoked()) throw new IllegalArgumentException("리프레시 토큰이 취소됨");
        if (stored.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(stored);
            throw new IllegalArgumentException("리프레시 토큰 만료");
        }

        Business user = stored.getUser();

        // rotate: 새 refresh token 발급, 기존 토큰 삭제(또는 마크)
        refreshTokenRepository.delete(stored);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), "USER");
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken();
        Date newRefreshExp = jwtTokenProvider.getClaims(newRefreshTokenValue).getExpiration();

        BusinessRefreshToken newRefresh = BusinessRefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
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
    public void logoutByUser(Business user) {
        refreshTokenRepository.deleteByUser(user);
    }
}



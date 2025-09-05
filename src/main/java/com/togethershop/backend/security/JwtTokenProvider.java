package com.togethershop.backend.security;

import com.togethershop.backend.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key secretKey;
    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(Long id, String role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + accessTokenExpiration);
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("roles", List.of("ROLE_" + role))
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        Date expire = new Date(now.getTime() + refreshTokenExpiration);
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 수정된 getAuthentication 메서드
    public Authentication getAuthentication(String token) {
        Long userId = getUserIdFromToken(token);

        // DB에서 실제 사용자 정보 로딩
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(userId);

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String resolveFromHeader(HttpServletRequest request) {
        return resolveToken(request);
    }

    public LocalDateTime getRefreshTokenExpiryDate() {
        return LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration));
    }
}
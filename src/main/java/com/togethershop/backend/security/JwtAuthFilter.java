package com.togethershop.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JwtAuthFilter.java (HTTP 요청용)
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        log.info("JwtAuthFilter invoked, path={}", request.getServletPath());

        String path = request.getServletPath();
        if (path.startsWith("/api/auth/")) {
            log.info("Skipping auth for path={}", path);
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtTokenProvider.resolveFromHeader(request);
            log.info("Resolved token: {}", token);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                log.info("Decoded userId: {}", userId);

                if (userId != null) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.info("Token is null or invalid");
            }
        } catch (Exception e) {
            log.error("JWT validation failed", e);
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }


}



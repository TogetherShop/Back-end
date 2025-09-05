package com.togethershop.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // WebSocket 세션 attribute에서 principal 가져오기
        Object principal = accessor.getSessionAttributes() != null
                ? accessor.getSessionAttributes().get("principal")
                : null;

        if (principal == null) {
            log.warn("인증되지 않은 사용자 접근 차단");
            throw new IllegalArgumentException("인증 실패: WebSocket 연결에 JWT 필요");
        }

        // UserDetails가 있다면 그 username을 로깅
        String username = principal instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : principal.toString();

        log.debug("STOMP 사용자 인증됨: {}", username);

        // SecurityContext에 Authentication 설정
        if (principal instanceof UserDetails userDetails) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        return message;
    }
}
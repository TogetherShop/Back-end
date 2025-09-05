package com.togethershop.backend.config;

import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.security.JwtHandshakeInterceptor;
import com.togethershop.backend.security.JwtTokenProvider;
import com.togethershop.backend.security.StompPrincipal;
import com.togethershop.backend.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws-chat")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("http://localhost:5173")  // CORS 허용
                .withSockJS();  // SockJS 지원
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic, /queue로 시작하는 destination을 브로커가 처리
        config.enableSimpleBroker("/topic", "/queue");
        // /app으로 시작하는 메시지를 애플리케이션이 처리
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);

                String token = accessor.getFirstNativeHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);

                    if (jwtTokenProvider.validateToken(token)) {
                        Long userId = jwtTokenProvider.getUserIdFromToken(token);
                        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(userId);
                        accessor.setUser(new StompPrincipal(userDetails));
                    }
                }
                return message;
            }
        });
    }
}

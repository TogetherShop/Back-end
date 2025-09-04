package com.togethershop.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.togethershop.backend.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    // ⭐ ObjectMapper 설정 개선 (LocalDateTime 처리)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern, StandardCharsets.UTF_8);
            String json = new String(message.getBody(), StandardCharsets.UTF_8);

            log.info("Redis 메시지 수신: channel={}", channel);
            log.debug("수신한 JSON: {}", json);

            ChatMessageDTO dto = objectMapper.readValue(json, ChatMessageDTO.class);

            // ⭐ roomId 추출 (channel에서)
            String roomId = channel.replace("chat.room.", "");

            // ⭐ 프론트엔드 구독 경로와 일치시키기
            String destination = "/topic/room/" + roomId;

            log.info("WebSocket 브로드캐스트: destination={}, messageId={}", destination, dto.getSenderId());

            messagingTemplate.convertAndSend(destination, dto);

            log.info("메시지 브로드캐스트 완료: roomId={}", roomId);

        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: pattern={}, error={}",
                    new String(pattern, StandardCharsets.UTF_8), e.getMessage(), e);
            // ⭐ 예외를 무시하지 말고 로깅
        }
    }
}

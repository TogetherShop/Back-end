package com.togethershop.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.togethershop.backend.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatPublisher {
    // ⭐ String 타입으로 변경 (JSON 직렬화를 위해)
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(ChatMessageDTO dto) {
        try {
            String channel = "chat.room." + dto.getRoomId();

            // ⭐ JSON으로 직렬화
            String jsonMessage = objectMapper.writeValueAsString(dto);

            log.info("Redis 발행 시작: channel={}, messageId={}", channel, dto.getSenderId());
            log.debug("발행할 JSON: {}", jsonMessage);

            redisTemplate.convertAndSend(channel, jsonMessage);

            log.info("Redis 발행 완료: channel={}", channel);

        } catch (Exception e) {
            log.error("Redis 메시지 발행 실패: roomId={}, error={}", dto.getRoomId(), e.getMessage(), e);
            throw new RuntimeException("메시지 발행 실패", e);
        }
    }
}
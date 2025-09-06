package com.togethershop.backend.config;

import com.togethershop.backend.service.RedisChatSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

// RedisSubscriberConfig.java (채널 구독 등록)
@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfig {

    private final RedisConnectionFactory cf;
    private final RedisChatSubscriber subscriber;

    @Bean
    public RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(cf);
        // 모든 방 채널 패턴 구독
        container.addMessageListener(subscriber, new PatternTopic("chat.room.*"));
        return container;
    }
}


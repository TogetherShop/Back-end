package com.togethershop.backend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {
    @Bean
    @Primary  // 이게 핵심! Spring의 기본 ObjectMapper를 덮어씀
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // JavaTimeModule 등록 (LocalDate, LocalDateTime 지원)
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 타임스탬프로 쓰지 않음
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null이 아닌 값만 포함
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }
}

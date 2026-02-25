package com.back.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()) // 날짜/시간 모듈 지원
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // [2026, 1, 26, 11, 19, 30] -> "2026-01-26T11:19:30" 형태로 바꿔줌
    }
}

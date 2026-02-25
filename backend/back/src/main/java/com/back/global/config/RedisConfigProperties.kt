package com.back.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.data.redis")
public record RedisConfigProperties(String host, int port) {}

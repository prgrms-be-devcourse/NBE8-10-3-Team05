package com.back.global.redis;

import org.springframework.data.redis.core.index.Indexed;

import jakarta.persistence.Id;
import lombok.*;

@Builder(toBuilder = true)
public record RedisEntity(@Id @Indexed Integer id, String nickname, String apiKey) {
    public static RedisEntity from(RedisCustomEntity dto) {
        return new RedisEntity(dto.id(), dto.nickname(), dto.apiKey());
    }
}

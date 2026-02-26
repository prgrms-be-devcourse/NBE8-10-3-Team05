package com.back.global.redis

import jakarta.persistence.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("example")
data class RedisCustomEntity(
    @Id
    val id: Int? = null,
    val nickname: String? = null,
    val apiKey: String? = null
) {}

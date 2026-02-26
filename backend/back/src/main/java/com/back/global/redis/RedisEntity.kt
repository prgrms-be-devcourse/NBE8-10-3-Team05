package com.back.global.redis

import jakarta.persistence.Id

data class RedisEntity(
    @Id
    val id: Int? = null,
    val nickname: String? = null,
    val apiKey: String? = null
) {
    companion object {
        @JvmStatic
        fun from(dto: RedisCustomEntity): RedisEntity {
            return RedisEntity(
                id = dto.id,
                nickname = dto.nickname,
                apiKey = dto.apiKey
            )
        }
    }
}

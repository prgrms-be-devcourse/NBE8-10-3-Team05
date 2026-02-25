package com.back.domain.auth.store

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.Optional

/**
 * Redis에 Refresh Token을 저장/조회/삭제하는 전용 저장소
 *
 * 기존 DB의 RefreshTokenRepository 역할을 대신함
 *
 * Redis 저장 구조:
 *  key   = "rt:{tokenHash}"
 *  value = memberId
 *  TTL   = refresh token 만료 시간
 */
@Repository
class RedisRefreshTokenStore(
    // Redis에 문자열(key-value) 형태로 접근하기 위한 템플릿
    private val redis: StringRedisTemplate
) {

    // Redis key prefix (refresh token 구분용)
    companion object {
        private const val PREFIX = "rt:"
    }

    /**
     * Refresh Token 저장
     *
     * @param tokenHash refresh token 원문을 해시한 값
     * @param memberId  어떤 회원의 토큰인지
     * @param ttl       만료 시간 (ex: 14일)
     */
    fun save(tokenHash: String, memberId: Long, ttl: Duration) {
        // 실제 Redis에 저장되는 key 예: rt:abc123hash
        val key = PREFIX + tokenHash

        // value는 memberId (문자열로 저장)
        redis.opsForValue().set(key, memberId.toString(), ttl)
        // TTL이 끝나면 Redis가 자동으로 삭제해줌
    }

    /**
     * Refresh Token으로 memberId 조회
     *
     * @param tokenHash refresh token 해시값
     * @return memberId (없으면 Optional.empty())
     */
    fun findMemberId(tokenHash: String): Optional<Long> {
        val key = PREFIX + tokenHash

        // Redis에서 value 조회
        val value = redis.opsForValue().get(key)

        // 없으면 유효하지 않은 refresh token
        if (value == null) {
            return Optional.empty()
        }

        // 있으면 memberId 반환
        return Optional.of(value.toLong())
    }

    /**
     * Refresh Token 삭제 (로그아웃 / 토큰 폐기 시 사용)
     *
     * @param tokenHash refresh token 해시값
     */
    fun delete(tokenHash: String) {
        val key = PREFIX + tokenHash
        redis.delete(key)
    }

    fun deleteAllByMemberId(memberId: Long) {
        val pattern = PREFIX + "*"

        redis.keys(pattern)?.forEach { key ->
            val value = redis.opsForValue().get(key)
            if (memberId.toString() == value) {
                redis.delete(key)
            }
        }
    }
}

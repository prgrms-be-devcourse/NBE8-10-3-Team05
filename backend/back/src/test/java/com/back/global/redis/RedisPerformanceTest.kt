package com.back.global.redis

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.system.measureTimeMillis

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestCacheConfig::class)
class RedisPerformanceTest {

    @Autowired
    private lateinit var realRedisExampleCustomRepository: RedisExampleCustomRepository

    @Autowired
    private lateinit var redisService: RedisService

    @Test
    @Transactional
    @DisplayName("DB 조회와 Redis 캐시 조회 속도 비교")
    fun comparePerformance() {
        val redisId = 1
        val testEntity = RedisCustomEntity(id = 1, nickname = "nick", apiKey = "key")

        // 데이터 준비
        realRedisExampleCustomRepository.save(testEntity)

        // 1. 첫 번째 조회 (Cache Miss - DB 접근)
        // Kotlin의 measureTimeMillis를 사용하여 시간을 더 깔끔하게 측정합니다.
        val dbTime = measureTimeMillis {
            redisService.getUser(redisId)
        }

        // 2. 두 번째 조회 (Cache Hit - Redis 접근)
        val redisTime = measureTimeMillis {
            redisService.getUser(redisId)
        }

        println("--------------------------------")
        println("DB 조회 시간: ${dbTime}ms")
        println("Redis 조회 시간: ${redisTime}ms")

        if (redisTime > 0) {
            println("성능 개선: ${dbTime.toDouble() / redisTime}배")
        }
        println("--------------------------------")

        // 일반적으로 로컬 환경이나 CI 환경에서도 Redis(In-memory)가 DB보다 빠름을 검증합니다.
        assertThat(redisTime).isLessThanOrEqualTo(dbTime)
    }
}

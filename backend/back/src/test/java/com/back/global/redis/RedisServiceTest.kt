package com.back.global.redis

import com.back.global.redis.RedisEntity.Companion.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*
import java.util.function.BooleanSupplier
import kotlin.math.abs

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
@Import(TestCacheConfig::class)
internal class RedisServiceTest {

    @Autowired
    private lateinit var cacheManager: CacheManager

    @MockitoBean
    private lateinit var redisExampleCustomRepository: RedisExampleCustomRepository

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    private val redisName = "redis"

    /**
     * 빌더 대신 Kotlin의 명명된 인자(Named Arguments)를 사용하여 엔티티 생성
     */
    private fun createTestEntity(id: Int, nickname: String, apiKey: String): RedisCustomEntity {
        return RedisCustomEntity(
            id = id,
            nickname = nickname,
            apiKey = apiKey
        )
    }

    private fun generateUniqueId(): Int = abs(UUID.randomUUID().hashCode())

    private fun cleanCache(redisId: Int) {
        val redisCache = cacheManager.getCache(redisName) ?: return
        redisCache.evict(redisId)

        // 삭제 확인 (동기화 대기)
        val physicalKey = "redis::$redisId"
        var retries = 0
        while (redisTemplate.hasKey(physicalKey) == true && retries < 10) {
            Thread.sleep(50)
            retries++
        }
    }

    @Test
    @DisplayName("Redis DB에 물리적으로 데이터가 저장되었는지 확인")
    fun checkPhysicalRedis() {
        val redisId = generateUniqueId()
        val testEntity = createTestEntity(redisId, "redis", "apiKey")
        val expectedKey = "redis::$redisId"

        // Redis에 데이터 저장
        redisTemplate.opsForValue().set(expectedKey, from(testEntity))

        // 저장 완료 대기
        waitForCondition({ redisTemplate.hasKey(expectedKey) == true }, 2000, 50)

        // 데이터 조회
        val actualValue = redisTemplate.opsForValue().get(expectedKey)

        // 검증
        assertThat(actualValue).isNotNull
        assertThat(actualValue).isInstanceOf(RedisEntity::class.java)

        val cachedEntity = actualValue as RedisEntity
        assertThat(cachedEntity.id).isEqualTo(redisId)
        assertThat(cachedEntity.nickname).isEqualTo("redis")

        // 삭제
        redisTemplate.delete(expectedKey)
        waitForCondition({ redisTemplate.hasKey(expectedKey) == false }, 2000, 50)
    }

    @Test
    @DisplayName("getUser 확인")
    fun getUser() {
        reset(redisExampleCustomRepository)

        val redisId = generateUniqueId()
        val physicalKey = "redis::$redisId"
        val testEntity = createTestEntity(redisId, "nick", "apiKey")

        `when`(redisExampleCustomRepository.findById(redisId))
            .thenReturn(Optional.of(testEntity))

        // 첫 번째 호출 (Cache Miss -> DB 접근)
        redisService.getUser(redisId)

        // 캐시 생성 완료 대기
        waitForCondition({ redisTemplate.hasKey(physicalKey) == true }, 2000, 50)

        // 두 번째 호출 (Cache Hit -> DB 접근 안 함)
        val result2 = redisService.getUser(redisId)

        // 검증
        assertThat(result2.id).isEqualTo(testEntity.id)
        assertThat(result2.nickname).isEqualTo(testEntity.nickname)
        verify(redisExampleCustomRepository, times(1)).findById(redisId)

        cleanCache(redisId)
    }

    @Test
    @DisplayName("updateUser 확인")
    fun updateUser() {
        val redisId = generateUniqueId()
        val physicalKey = "redis::$redisId"
        val oldEntity = createTestEntity(redisId, "nick", "oldApiKey")
        val newEntity = createTestEntity(redisId, "nick", "newApiKey")

        `when`(redisExampleCustomRepository.findById(redisId))
            .thenReturn(Optional.of(oldEntity))
            .thenReturn(Optional.of(newEntity))

        redisService.getUser(redisId)

        val result = redisService.updateUser(redisId, "newApiKey")

        waitForCondition({
            val value = redisTemplate.opsForValue().get(physicalKey)
            if (value is RedisEntity) value.apiKey == "newApiKey" else false
        }, 500, 50)

        assertThat(result.apiKey).isEqualTo("newApiKey")

        val actualValue = redisTemplate.opsForValue().get(physicalKey) as? RedisEntity
        assertThat(actualValue?.apiKey).isEqualTo("newApiKey")

        cleanCache(redisId)
    }

    @Test
    @DisplayName("deleteUser 확인")
    fun deleteUser() {
        val redisId = generateUniqueId()
        val physicalKey = "redis::$redisId"
        val testEntity = createTestEntity(redisId, "nick", "apiKey")

        `when`(redisExampleCustomRepository.findById(redisId))
            .thenReturn(Optional.of(testEntity))

        redisService.getUser(redisId)
        waitForCondition({ redisTemplate.hasKey(physicalKey) == true }, 2000, 50)

        assertThat(redisTemplate.hasKey(physicalKey)).isTrue()

        // 삭제 실행
        redisService.deleteUser(redisId)

        verify(redisExampleCustomRepository, times(1)).deleteById(redisId)
        waitForCondition({ redisTemplate.hasKey(physicalKey) == false }, 2000, 50)

        assertThat(redisTemplate.hasKey(physicalKey)).isFalse()
    }

    @Test
    @DisplayName("자원공유 문제시 무조건 충돌나는 테스트")
    fun forceFailureTest() {
        val redisId = generateUniqueId()
        val physicalKey = "redis::$redisId"

        waitForCondition({ redisTemplate.hasKey(physicalKey) == false }, 500, 50)

        if (redisTemplate.hasKey(physicalKey) == true) {
            throw RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.")
        }

        val testEntity = createTestEntity(redisId, "nick", "apiKey")
        redisTemplate.opsForValue().set(physicalKey, from(testEntity))

        waitForCondition({ redisTemplate.hasKey(physicalKey) == true }, 500, 50)

        Thread.sleep(1000)

        redisTemplate.delete(physicalKey)
        waitForCondition({ redisTemplate.hasKey(physicalKey) == false }, 500, 50)
    }

    // 헬퍼 메서드: 람다 표현식으로 더 간결하게 사용 가능
    private fun waitForCondition(condition: BooleanSupplier, timeoutMs: Long, pollIntervalMs: Long) {
        val startTime = System.currentTimeMillis()
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw AssertionError("조건이 시간 내에 충족되지 않았습니다.")
            }
            Thread.sleep(pollIntervalMs)
        }
    }
}

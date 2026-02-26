package com.back.global.redis

import com.back.global.redis.RedisEntity.Companion.from
import org.junit.jupiter.api.*
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Execution(ExecutionMode.CONCURRENT) // 병렬 실행 활성화
internal class RedisServiceParallelTest {

    @Autowired
    private lateinit var cacheManager: CacheManager

    @MockitoBean
    private lateinit var redisExampleCustomRepository: RedisExampleCustomRepository

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    private val redisName = "redis"

    @BeforeEach
    fun setUp() {
        Mockito.reset(redisExampleCustomRepository)
    }

    private fun createTestEntity(id: Int, nickname: String, apiKey: String): RedisCustomEntity {
        // 빌더 대신 Kotlin Named Arguments 사용
        return RedisCustomEntity(
            id = id,
            nickname = nickname,
            apiKey = apiKey
        )
    }

    private fun generateUniqueId(): Int = abs(UUID.randomUUID().hashCode())

    // ============================================
    // 멀티코어 충돌 테스트 (고정 키 사용)
    // ============================================
    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #1")
    fun multiCoreConflictTest1() {
        // ❌ 고정 키 사용 → 다른 테스트와 충돌 발생!
        val physicalKey = "redis::SHARED_KEY"

        if (redisTemplate.hasKey(physicalKey) == true) {
            conflictCount.incrementAndGet()
            println("❌ [Test #1] 충돌 발생! 다른 테스트가 이미 키를 사용 중입니다.")
            throw RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.")
        }

        val testEntity = createTestEntity(1, "nick", "apiKey")
        redisTemplate.opsForValue().set(physicalKey, from(testEntity))

        println("✅ [Test #1] 키 생성 완료, 1초 대기 시작...")
        Thread.sleep(1000) // 다른 테스트가 접근할 시간 제공

        redisTemplate.delete(physicalKey)
        println("✅ [Test #1] 키 삭제 완료")
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #2")
    fun multiCoreConflictTest2() {
        val physicalKey = "redis::SHARED_KEY"

        if (redisTemplate.hasKey(physicalKey) == true) {
            conflictCount.incrementAndGet()
            println("❌ [Test #2] 충돌 발생! 다른 테스트가 이미 키를 사용 중입니다.")
            throw RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.")
        }

        val testEntity = createTestEntity(2, "nick2", "apiKey2")
        redisTemplate.opsForValue().set(physicalKey, from(testEntity))

        println("✅ [Test #2] 키 생성 완료, 1초 대기 시작...")
        Thread.sleep(1000)

        redisTemplate.delete(physicalKey)
        println("✅ [Test #2] 키 삭제 완료")
    }

    // (Test #3, #4, #5는 동일한 구조이므로 생략 가능하나 마이그레이션 규칙을 준수합니다)
    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #3")
    fun multiCoreConflictTest3() {
        val physicalKey = "redis::SHARED_KEY"
        if (redisTemplate.hasKey(physicalKey) == true) {
            conflictCount.incrementAndGet()
            throw RuntimeException("병렬 경합 발생!")
        }
        redisTemplate.opsForValue().set(physicalKey, from(createTestEntity(3, "nick3", "apiKey3")))
        Thread.sleep(1000)
        redisTemplate.delete(physicalKey)
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #4")
    fun multiCoreConflictTest4() {
        val physicalKey = "redis::SHARED_KEY"
        if (redisTemplate.hasKey(physicalKey) == true) {
            conflictCount.incrementAndGet()
            throw RuntimeException("병렬 경합 발생!")
        }
        redisTemplate.opsForValue().set(physicalKey, from(createTestEntity(4, "nick4", "apiKey4")))
        Thread.sleep(1000)
        redisTemplate.delete(physicalKey)
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #5")
    fun multiCoreConflictTest5() {
        val physicalKey = "redis::SHARED_KEY"
        if (redisTemplate.hasKey(physicalKey) == true) {
            conflictCount.incrementAndGet()
            throw RuntimeException("병렬 경합 발생!")
        }
        redisTemplate.opsForValue().set(physicalKey, from(createTestEntity(5, "nick5", "apiKey5")))
        Thread.sleep(1000)
        redisTemplate.delete(physicalKey)
    }

    // ============================================
    // 대조군: Unique ID 사용 (안전한 테스트)
    // ============================================
    @Test
    @DisplayName("멀티코어 안전 테스트 - Unique ID #1")
    fun multiCoreSafeTest1() {
        val redisId = generateUniqueId() // ✅ 유니크 키
        val physicalKey = "redis::$redisId"

        val testEntity = createTestEntity(redisId, "safe1", "key1")
        redisTemplate.opsForValue().set(physicalKey, from(testEntity))

        println("✅ [Safe #1] 유니크 키 사용, 충돌 없음")
        Thread.sleep(500)

        redisTemplate.delete(physicalKey)
    }

    @Test
    @DisplayName("멀티코어 안전 테스트 - Unique ID #2")
    fun multiCoreSafeTest2() {
        val redisId = generateUniqueId()
        val physicalKey = "redis::$redisId"
        redisTemplate.opsForValue().set(physicalKey, from(createTestEntity(redisId, "safe2", "key2")))
        println("✅ [Safe #2] 유니크 키 사용, 충돌 없음")
        Thread.sleep(500)
        redisTemplate.delete(physicalKey)
    }

    @Test
    @DisplayName("멀티코어 안전 테스트 - Unique ID #3")
    fun multiCoreSafeTest3() {
        val redisId = generateUniqueId()
        val physicalKey = "redis::$redisId"
        redisTemplate.opsForValue().set(physicalKey, from(createTestEntity(redisId, "safe3", "key3")))
        println("✅ [Safe #3] 유니크 키 사용, 충돌 없음")
        Thread.sleep(500)
        redisTemplate.delete(physicalKey)
    }

    companion object {
        // 충돌 카운터
        private val conflictCount = AtomicInteger(0)

        // ============================================
        // 충돌 통계 확인
        // ============================================
        @JvmStatic
        @AfterAll
        fun printConflictStats() {
            println("\n========================================")
            println("멀티코어 테스트 결과")
            println("========================================")
            println("총 충돌 발생 횟수: ${conflictCount.get()}")
            println("예상: 고정 키 사용 시 여러 테스트가 충돌")
            println("실제: Unique ID 사용 시 충돌 없음")
            println("========================================\n")
        }
    }
}

package com.back.global.redis

import com.back.global.redis.RedisEntity.Companion.from
import lombok.RequiredArgsConstructor
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RedisService(
    private val redisExampleCustomRepository: RedisExampleCustomRepository
) {

    /**
     * Redis에는 user:1 키-데이터로 저장됩니다.
     * Redis에 값이 있으면 메서드를 실행하지 않고 바로 반환합니다
     * Redis에 값이 없으면 메서드를 실행하고, 그 결과를 Redis에 저장합니다.
     * key와 parameter가 일치해야함. key = "#redisId" == getUser(Integer redisId)
     */
    @Cacheable(value = ["redis"], key = "#redisId")
    fun getUser(redisId: Int): RedisEntity {
        // Redis에 데이터가 없으면 이 로직이 실행되어 DB를 조회합니다.
        val entity = redisExampleCustomRepository.findById(redisId).orElseThrow()
        return from(entity)
    }

    /**
     * @Cacheable과 달리 메서드를 항상 실행하며, 실행 결과를 Redis에 덮어씁니다.
     */
    @CachePut(value = ["redis"], key = "#redisId")
    fun updateUser(redisId: Int, newApiKey: String?): RedisEntity {
        // 캐시가 이미 존재한다면 여기서 repository에서 가져오는게 아니라
        // 캐시에서 값을 꺼내온다.
        val redisEntity = redisExampleCustomRepository.findById(redisId).orElseThrow()

        // 기존 toBuilder().build() 대신 Kotlin의 copy() 활용
        val updated = redisEntity.copy(apiKey = newApiKey)

        // redisEntity.update();
        redisExampleCustomRepository.save(updated)

        // 수정된 객체를 반환하면 Redis의 기존 캐시가 이 값으로 교체됩니다.
        return from(updated)
    }

    @CacheEvict(value = ["redis"], key = "#redisId")
    fun deleteUser(redisId: Int) {
        // DB에서 삭제
        redisExampleCustomRepository.deleteById(redisId)
        // 메서드가 성공적으로 끝나면 Redis에 있던 "user::userId" 키도 삭제됩니다.
    }
}

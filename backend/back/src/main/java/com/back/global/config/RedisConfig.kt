package com.back.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig(
    private val redisConfigProperties: RedisConfigProperties
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(redisConfigProperties.host, redisConfigProperties.port)
    }

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        // 오직 @Cacheable (Spring Cache) 계열에만 적용되는 설정
        val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json())
            )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfig)
            .transactionAware()
            .build()
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        // apply 스코프 함수를 사용하여 설정 로직을 그룹화합니다.
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)

            // 캐시 설정에서 사용했던 것과 동일한 직렬화 방식을 적용
            keySerializer = StringRedisSerializer()
            valueSerializer = RedisSerializer.json()

            // Redis의 Hash 데이터 타입을 사용할 때 적용
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = RedisSerializer.json()

            // 초기화 명시
            afterPropertiesSet()
        }
    }
}

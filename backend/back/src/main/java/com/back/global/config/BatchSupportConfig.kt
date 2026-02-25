package com.back.global.config

import io.netty.channel.ConnectTimeoutException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.client.ResourceAccessException
import java.net.SocketTimeoutException
import java.time.Duration

@Configuration
class BatchSupportConfig {
    @Bean
    fun taskExecutor(): AsyncTaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 8
            maxPoolSize = 8
            setThreadNamePrefix("batch-thread-")
            initialize()
        }

    @Bean
    fun crawlingTaskExecutor(): AsyncTaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 4
            queueCapacity = 100
            setThreadNamePrefix("crawler-")
            initialize()
        }

    @Bean
    fun retryPolicy(): RetryPolicy{
        return RetryPolicy.builder()
            .maxRetries(3) // 총 4번 실행 (최초 1 + 재시도 3)
            .delay(Duration.ofSeconds(2))
            .includes(
                SocketTimeoutException::class.java,
                ResourceAccessException::class.java,
                ConnectTimeoutException::class.java
            )
            .build()
    }
}

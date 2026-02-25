package com.back.global.config;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.ResourceAccessException;

import io.netty.channel.ConnectTimeoutException;

@Configuration
public class BatchSupportConfig {
    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8); // 기본 스레드 수 mysql-hikari의 기본설명은 10개
        executor.setMaxPoolSize(8); // 최대 스레드 수 일이 너무 많아져서 대기 큐(Queue)까지 꽉 차면, 일꾼을 추가로 더 뽑습니다
        // executor.setQueueCapacity(50);    // 대기 큐 기본 일꾼 10명이 모두 일하고 있다면, 새로 들어온 작업(청크 단위)은 이 대기 줄에 서서 기다립니다. 50개가 넘게
        // 줄을 서면 그때서야 MaxPoolSize만큼 일꾼을 추가로 투입
        executor.setThreadNamePrefix("batch-thread-"); // 로그를 찍을 때 [batch-thread-1], [batch-thread-2] 처럼 출력
        executor.initialize();
        return executor;
    }

    @Bean
    public RetryPolicy retryPolicy() {
        return RetryPolicy.builder()
                .maxRetries(4) // 최초 1 + 재시도 3번
                .delay(Duration.ofSeconds(2)) // 2초 대기
                .includes(Set.of(
                        SocketTimeoutException.class, ResourceAccessException.class, ConnectTimeoutException.class))
                .build();
    }

    @Bean
    public AsyncTaskExecutor crawlingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);

        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("crawler-");

        // 서버 종료 시 진행 중인 크롤링은 마무리하고 닫도록 설정
        // executor.setWaitForTasksToCompleteOnShutdown(true);
        // executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}

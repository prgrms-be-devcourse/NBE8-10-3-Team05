package com.back.global.springBatch.center

import com.back.domain.welfare.center.center.entity.Center
import jakarta.persistence.EntityManagerFactory
import lombok.RequiredArgsConstructor
import org.springframework.batch.infrastructure.item.database.JpaItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CenterApiItemWriter(
    private val entityManagerFactory: EntityManagerFactory
) {

    @Bean
    fun centerJpaItemWriter(): JpaItemWriter<Center> {
        return JpaItemWriterBuilder<Center>()
            .entityManagerFactory(entityManagerFactory)
            .usePersist(true) // 신규 데이터 저장 시 성능 최적화 (필요에 따라 설정)
            .build()
    }
}

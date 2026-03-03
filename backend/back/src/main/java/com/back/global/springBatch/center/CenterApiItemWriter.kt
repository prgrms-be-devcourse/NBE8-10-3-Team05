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
            // 신규 데이터 저장 시 성능 최적화 필요시 수정
            // center는 api fetch시 id까지 받아오기때문에 근야 jpa merge수행
            .usePersist(false)
            .build()
    }
}

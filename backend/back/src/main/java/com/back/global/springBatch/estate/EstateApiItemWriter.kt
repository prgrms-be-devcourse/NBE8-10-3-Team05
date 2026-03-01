package com.back.global.springBatch.estate

import com.back.domain.welfare.estate.entity.Estate
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.infrastructure.item.database.JpaItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EstateApiItemWriter(
    private val entityManagerFactory: EntityManagerFactory
) {

    @Bean
    fun estateJpaItemWriter(): JpaItemWriter<Estate> {
        return JpaItemWriterBuilder<Estate>()
            .entityManagerFactory(entityManagerFactory)
            //삭제 후 완전 재설치이기 때문에 persist를 true로 하여 불필요한 merge연산 제거
            .usePersist(true)
            .build()
    }
}

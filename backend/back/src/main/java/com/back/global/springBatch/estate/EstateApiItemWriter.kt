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
            .build()
    }
}

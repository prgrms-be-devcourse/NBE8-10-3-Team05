package com.back.global.springBatch.lawyer

import com.back.domain.welfare.center.lawyer.entity.Lawyer
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.infrastructure.item.database.JpaItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LawyerApiItemWriter(private val entityManagerFactory: EntityManagerFactory) {
    @Bean
    fun lawyerJpaItemWriter(): JpaItemWriter<Lawyer> {
        return JpaItemWriterBuilder<Lawyer>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }
}

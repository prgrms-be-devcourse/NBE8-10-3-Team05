package com.back.global.springBatch.lawyer;

import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LawyerApiItemWriter {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public JpaItemWriter<Lawyer> lawyerJpaItemWriter() {
        return new JpaItemWriterBuilder<Lawyer>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}

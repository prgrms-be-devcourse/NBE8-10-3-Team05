package com.back.global.springBatch.policy;

import java.util.Arrays;

import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.back.domain.welfare.policy.entity.Policy;
import com.back.global.springBatch.elasticSearch.PolicyEsItemWriter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PolicyApiItemWriter {
    private final EntityManagerFactory entityManagerFactory;
    private final PolicyEsItemWriter policyEsItemWriter;

    // 스프링이 트랜잭션과 연결된 '가짜(Proxy) 매니저'를 넣어줍니다.
    private final EntityManager entityManager;

    @Bean
    public ItemWriter<Policy> policyJpaItemWriter() {
        return items -> {
            for (Policy policy : items) {
                if (policy.getId() == null) {
                    entityManager.persist(policy); // 직접 영속화 (ID 채워짐)
                } else {
                    entityManager.merge(policy); // 기존 데이터면 업데이트
                }
            }
            entityManager.flush(); // DB에 즉시 반영하여 ID 확정
        };
    }

    // 부분 실패: DB 저장은 성공했는데 ES 전송 중 네트워크 오류가 나면, Spring Batch는 해당 **Chunk 전체를 실패(Rollback)**로 처리합니다.
    @Bean
    public CompositeItemWriter<Policy> compositePolicyWriter() {
        return new CompositeItemWriterBuilder<Policy>()
                .delegates(Arrays.asList(policyJpaItemWriter(), policyEsItemWriter)) // DB -> ES 순서
                .build();
    }
}

package com.back.global.springBatch.policy

import com.back.domain.welfare.policy.entity.Policy
import com.back.global.springBatch.elasticSearch.PolicyEsItemWriter
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import lombok.RequiredArgsConstructor
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter
import org.springframework.batch.infrastructure.item.support.builder.CompositeItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class PolicyApiItemWriter(
    private val entityManager: EntityManager,       // 생성자 주입 (Proxy EntityManager)
    private val policyEsItemWriter: PolicyEsItemWriter // 생성자 주입
) {

    @Bean
    fun policyJpaItemWriter(): ItemWriter<Policy> {
        return ItemWriter { chunk ->
            for (policy in chunk.items) {
                if (policy.id == null) {
                    entityManager.persist(policy) // 신규 저장 (ID 생성됨)
                } else {
                    entityManager.merge(policy)   // 기존 데이터 업데이트
                }
            }
            // Chunk 단위 트랜잭션 종료 전 명시적으로 flush하여 DB와 동기화
            entityManager.flush()
        }
    }

    // 부분 실패: DB 저장은 성공했는데 ES 전송 중 네트워크 오류가 나면, Spring Batch는 해당 **Chunk 전체를 실패(Rollback)**로 처리합니다.
    @Bean
    fun compositePolicyWriter(): CompositeItemWriter<Policy> {
        return CompositeItemWriterBuilder<Policy>()
            .delegates(listOf(policyJpaItemWriter(), policyEsItemWriter))
            .build()
    }
}

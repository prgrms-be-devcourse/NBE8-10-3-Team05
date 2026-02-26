package com.back.global.springBatch.policy

import co.elastic.clients.elasticsearch.ElasticsearchClient
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.Pagging
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.domain.welfare.policy.service.PolicyApiClient
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter
import org.springframework.batch.test.JobOperatorTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBatchTest
@SpringBootTest
@Transactional
@ActiveProfiles("test") // 프로필 명시
@Disabled // 필요 시 해제
internal class PolicyApiItemWriterTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var jobOperatorTestUtils: JobOperatorTestUtils

    @Autowired
    private lateinit var policyRepository: PolicyRepository

    @Autowired
    private lateinit var esClient: ElasticsearchClient

    @MockitoBean
    private lateinit var policyApiClient: PolicyApiClient

    @Autowired
    private lateinit var policyApiItemProcessor: PolicyApiItemProcessor

    @Autowired
    private lateinit var policyApiItemReader: PolicyApiItemReader

    @Autowired
    private lateinit var policyCompositeItemWriter: CompositeItemWriter<Policy> // Policy?에서 Policy로 수정

    @Autowired
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun clearMetadata() {
        policyRepository.deleteAllInBatch()
        entityManager.flush()
        entityManager.clear()

        // ES 인덱스 초기화 (Kotlin 람다 스타일로 훨씬 깔끔해짐)
        runCatching {
            esClient.indices().delete { d -> d.index("policy") }
        }
    }

    @Test
    @DisplayName("batch시 실제 es동기화가 진행되는지")
    fun compositeWriter() {
        // given
        val mockResponse = createMockResponse()

        `when`(policyApiClient.fetchPolicyPage(any(), anyInt(), anyInt()))
            .thenReturn(mockResponse)

        // when
        // startJob()은 등록된 Job을 실행합니다.
        val jobExecution = jobOperatorTestUtils.startJob()

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        // DB 확인
        val dbCount = policyRepository.count()
        assertThat(dbCount).isGreaterThan(0)

        // ES Refresh
        esClient.indices().refresh { r -> r.index("policy") }

        // ES Count 확인
        val response = esClient.count { c -> c.index("policy") }
        assertThat(response.count()).isEqualTo(dbCount)
    }

    private fun createMockResponse(): PolicyFetchResponseDto {
        val item1 = createPolicyItem("PLCY001", "정책1", "SBIZ001")
        val item2 = createPolicyItem("PLCY002", "정책2", "SBIZ002")

        val paging = Pagging(2, 1, 100)
        val result = PolicyFetchResponseDto.Result(paging, listOf(item1, item2))

        return PolicyFetchResponseDto(0, "SUCCESS", result)
    }

    // 반복되는 Mock 객체 생성을 위한 헬퍼 함수
    private fun createPolicyItem(id: String, name: String, bizId: String) = PolicyItem(
        id, null, null, null, null, null, null, null, name, "키워드",
        "설명", null, null, "지원내용", null, "주관기관", null, null,
        "운영기관", null, null, "001", "002", "20240101", "20241231",
        null, "온라인", null, "http://apply", "서류", null, null, null,
        null, null, "20", "30", "N", "001", "002", "0", "5000", null,
        null, null, null, null, null, null, null, "12345", null, "null",
        "null", "JOB001", "SCH001", "20240101", null, null, bizId
    )
}

package com.back.global.springBatch.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.service.PolicyApiClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.persistence.EntityManager;

@SpringBatchTest
@SpringBootTest
@Transactional
@Disabled
class PolicyApiItemWriterTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Test
    void contextLoads() {
        assertThat(jobOperatorTestUtils).isNotNull();
    }

    @Autowired
    private PolicyRepository policyRepository; // JPA 확인용

    @Autowired
    private ElasticsearchClient esClient; // ES 확인용

    @MockitoBean
    private PolicyApiClient policyApiClient;

    @Autowired
    private PolicyApiItemProcessor policyApiItemProcessor;

    @Autowired
    private PolicyApiItemReader policyApiItemReader;

    @Autowired // 실제 객체 사용
    private CompositeItemWriter<Policy> policyCompositeItemWriter; // Writer 클래스가 아닌 빈 타입을 Mocking

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearMetadata() {
        policyRepository.deleteAllInBatch();

        // 2. JPA 영속성 컨텍스트 초기화 (이게 없으면 1번 결과가 캐시 때문에 안 보임)
        entityManager.flush();
        entityManager.clear();

        // 3. ES 인덱스 초기화 (데이터만 지우거나 인덱스 삭제 후 재생성)
        try {
            esClient.indices().delete(d -> d.index("policy"));
            // 인덱스 생성 로직이 별도로 있다면 여기서 호출
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("batch시 실제 es동기화가 진행되는지")
    void compositeWriter() throws Exception {
        PolicyFetchResponseDto mockResponse = createMockresponse();

        when(policyApiClient.fetchPolicyPage(any(), anyInt(), anyInt())).thenReturn(mockResponse);

        // when
        JobExecution jobExecution = jobOperatorTestUtils.startJob();

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // DB에 저장되었는지 확인
        long dbCount = policyRepository.count();
        assertThat(dbCount).isGreaterThan(0);

        esClient.indices().refresh(r -> r.index("policy"));

        // ES에 저장되었는지 확인 (동기화 시간이 필요할 수 있으므로 잠시 대기하거나 refresh 필요)
        var response = esClient.count(c -> c.index("policy"));
        assertThat(response.count()).isEqualTo(dbCount);
    }

    // rollback test

    private PolicyFetchResponseDto createMockresponse() {
        PolicyFetchResponseDto.PolicyItem item1 = new PolicyFetchResponseDto.PolicyItem(
                "PLCY001",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "정책1",
                "키워드",
                "설명",
                null,
                null,
                "지원내용",
                null,
                "주관기관",
                null,
                null,
                "운영기관",
                null,
                null,
                "001",
                "002",
                "20240101",
                "20241231",
                null,
                "온라인",
                null,
                "http://apply",
                "서류",
                null,
                null,
                null,
                null,
                null,
                "20",
                "30",
                "N",
                "001",
                "002",
                "0",
                "5000",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "12345",
                null,
                "null",
                "null",
                "JOB001",
                "SCH001",
                "20240101",
                null,
                null,
                "SBIZ001");

        PolicyFetchResponseDto.PolicyItem item2 = new PolicyFetchResponseDto.PolicyItem(
                "PLCY002",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "정책2",
                "키워드",
                "설명",
                null,
                null,
                "지원내용",
                null,
                "주관기관",
                null,
                null,
                "운영기관",
                null,
                null,
                "001",
                "002",
                "20240101",
                "20241231",
                null,
                "온라인",
                null,
                "http://apply",
                "서류",
                null,
                null,
                null,
                null,
                null,
                "20",
                "30",
                "N",
                "001",
                "002",
                "0",
                "5000",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "12345",
                null,
                "null",
                "null",
                "JOB001",
                "SCH001",
                "20240101",
                null,
                null,
                "SBIZ002");

        PolicyFetchResponseDto.Pagging pagging = new PolicyFetchResponseDto.Pagging(2, 1, 100);
        PolicyFetchResponseDto.Result result = new PolicyFetchResponseDto.Result(pagging, List.of(item1, item2));

        return new PolicyFetchResponseDto(0, "SUCCESS", result);
    }
}

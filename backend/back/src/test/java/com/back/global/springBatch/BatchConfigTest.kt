package com.back.global.springBatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.domain.welfare.center.center.service.CenterApiService;
import com.back.global.springBatch.center.CenterApiItemProcessor;
import com.back.global.springBatch.center.CenterApiItemReader;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class BatchConfigTest {

    @Autowired
    @Qualifier("fetchApiJob") private Job fetchAPiJob; // 이 'job'이 주입되어야 jobOperatorTestUtils 내부가 채워집니다.

    // test : contextLoads 에서 확인가능하듯이, 실제로는 정상적으로 주입받음에도
    // IDE는 오류로 판단하기때문에 @SuppressWarnings
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Test
    void contextLoads() {
        assertThat(jobRepositoryTestUtils).isNotNull();
        assertThat(jobOperatorTestUtils).isNotNull();
    }

    @MockitoBean
    private CenterApiService centerApiService;

    @MockitoBean
    CenterApiItemProcessor centerApiItemProcessor;

    @MockitoBean
    CenterApiItemReader centerApiItemReader;

    @MockitoBean
    private JpaItemWriter<Center> centerJpaItemWriter; // Writer 클래스가 아닌 빈 타입을 Mocking

    @BeforeEach
    void clearMetadata() {
        jobOperatorTestUtils.setJob(fetchAPiJob);
        CenterApiResponseDto mockResponse = new CenterApiResponseDto(1, 1, 1, 1, 1, List.of());
        // 진짜 api를 호출하지 않도록 잡아준다.
        // enient()를 붙이면 "안 써도 괜찮으니까 일단 설정해 둬"라는 뜻이 되어 에러 없이 통과
        lenient().when(centerApiService.fetchCenter(any())).thenReturn(mockResponse);

        jobRepositoryTestUtils.removeJobExecutions(); // 이전 테스트 기록 삭제
        Mockito.reset(centerApiItemReader, centerApiItemProcessor, centerJpaItemWriter);
    }

    @Test
    void retryTest() throws Exception {
        // given: 2번 실패 후 3번째 성공하는 로직을 Mockito로 설정
        given(centerApiItemProcessor.process(any()))
                .willThrow(new SocketTimeoutException("1차 실패"))
                .willThrow(new SocketTimeoutException("2차 실패"))
                .willReturn(new Center()); // 3차 성공

        given(centerApiItemReader.read())
                .willReturn(new CenterApiResponseDto.CenterDto(1, "", "", "", "", "", ""))
                .willReturn(null); // 한 건만 처리 null을 반환하는 것은 Spring Batch에게 "이제 읽을 데이터가 없으니 이 단계를 끝내라"라고 알리는 신호

        // when
        JobExecution jobExecution = jobOperatorTestUtils.startStep("fetchCenterApiStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        then(centerApiItemProcessor)
                .should(times(3)) // 1차 실패 + 2차 실패 + 3차 성공 = 총 3번 호출
                .process(any());

        assertThat(jobExecution.getStepExecutions().iterator().next().getWriteCount())
                .isEqualTo(1);
    }

    @Test
    void multiThreadStepTest() throws Exception {
        // given: 충분한 아이템 (여러 chunk 생성)
        AtomicInteger counter = new AtomicInteger(0);

        given(centerApiItemReader.read()).willAnswer(invocation -> {
            int i = counter.incrementAndGet();
            return i <= 100 ? new CenterApiResponseDto.CenterDto(i, "", "", "", "", "", "") : null;
        });

        // processor에서 살짝 sleep → 스레드 분산 유도
        Set<String> threadNames = Collections.synchronizedSet(new HashSet<>());

        given(centerApiItemProcessor.process(any())).willAnswer(invocation -> {
            threadNames.add(Thread.currentThread().getName());
            Thread.sleep(50); // 🔥 없으면 한 스레드로 끝날 수도 있음
            return new Center();
        });

        // writer는 그냥 통과
        doNothing().when(centerJpaItemWriter).write(any());

        // when
        JobExecution jobExecution = jobOperatorTestUtils.startStep("fetchCenterApiStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(threadNames.size()).as("멀티스레드로 실행되어야 함").isGreaterThan(1);

        System.out.println("사용된 스레드: " + threadNames);
    }
}

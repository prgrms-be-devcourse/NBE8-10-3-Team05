package com.back.global.springBatch

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import com.back.domain.welfare.center.center.entity.Center
import com.back.domain.welfare.center.center.service.CenterApiService
import com.back.global.springBatch.center.CenterApiItemProcessor
import com.back.global.springBatch.center.CenterApiItemReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.infrastructure.item.database.JpaItemWriter
import org.springframework.batch.test.JobOperatorTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
internal class BatchConfigTest {

    @Autowired
    @Qualifier("fetchApiJob")
    private lateinit var fetchAPiJob: Job

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var jobOperatorTestUtils: JobOperatorTestUtils

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils

    @MockitoBean
    private lateinit var centerApiService: CenterApiService

    @MockitoBean
    private lateinit var centerApiItemProcessor: CenterApiItemProcessor

    @MockitoBean
    private lateinit var centerApiItemReader: CenterApiItemReader

    @MockitoBean
    private lateinit var centerJpaItemWriter: JpaItemWriter<Center> // 제네릭 Any 제약 대응

    @BeforeEach
    fun clearMetadata() {
        jobOperatorTestUtils.setJob(fetchAPiJob)

        val mockResponse = CenterApiResponseDto(1, 1, 1, 1, 1, mutableListOf())

        Mockito.lenient()
            .`when`(centerApiService.fetchCenter(any()))
            .thenReturn(mockResponse)

        jobRepositoryTestUtils.removeJobExecutions()
        Mockito.reset(centerApiItemReader, centerApiItemProcessor, centerJpaItemWriter)
    }

    @Test
    fun retryTest() {
        // given
        given(centerApiItemProcessor.process(any()))
            .willThrow(RuntimeException(SocketTimeoutException("1차 실패"))) // Checked Exception 우회
            .willThrow(RuntimeException(SocketTimeoutException("2차 실패")))
            .willReturn(Center())

        given(centerApiItemReader.read())
            .willReturn(CenterDto(1, "", "", "", "", "", ""))
            .willReturn(null)

        // when
        val jobExecution = jobOperatorTestUtils.startStep("fetchCenterApiStep")

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        then(centerApiItemProcessor)
            .should(times(3))
            .process(any())

        assertThat(jobExecution.stepExecutions.iterator().next().writeCount).isEqualTo(1)
    }

    @Test
    fun multiThreadStepTest() {
        // given
        val counter = AtomicInteger(0)
        val threadNames = Collections.synchronizedSet(HashSet<String>())

        given(centerApiItemReader.read()).willAnswer {
            val i = counter.incrementAndGet()
            if (i <= 100) CenterDto(i, "", "", "", "", "", "") else null
        }

        given(centerApiItemProcessor.process(any())).willAnswer {
            threadNames.add(Thread.currentThread().name)
            Thread.sleep(50)
            Center()
        }

        // Writer Mocking (Chunk 타입 매칭 주의)
        doNothing().`when`(centerJpaItemWriter).write(any())

        // when
        val jobExecution = jobOperatorTestUtils.startStep("fetchCenterApiStep")

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(threadNames.size).`as`("멀티스레드로 실행되어야 함").isGreaterThan(1)

        println("사용된 스레드: $threadNames")
    }
}

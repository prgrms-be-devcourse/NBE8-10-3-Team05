package com.back.global.springBatch

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.launch.JobOperator
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension::class)
internal class BatchJobLauncherTest {

    @Mock
    private lateinit var jobOperator: JobOperator

    @Mock
    private lateinit var fetchApiJob: Job

    @InjectMocks
    private lateinit var batchJobLauncher: BatchJobLauncher

    @Test
    @DisplayName("fetchApiJob이 잘 launch되는지")
    fun t1() {
        // given
        `when`(fetchApiJob.name).thenReturn("testJob")

        // when
        batchJobLauncher.runJob()

        // then
        // verify 시에도 굳이 클래스 타입을 명시하지 않아도 타입 추론으로 해결됩니다.
        verify(jobOperator).start(any<Job>(), any())
    }
}

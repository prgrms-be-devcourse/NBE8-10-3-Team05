package com.back.global.springBatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class BatchJobLauncherTest {
    @Mock
    private JobOperator jobOperator; // 가짜 객체 생성 (1)

    @Mock
    private Job fetchApiJob; // 가짜 객체 생성 (2)

    @InjectMocks
    private BatchJobLauncher batchJobLauncher; // 1 + 2 여기 주입

    @Test
    @DisplayName("fetchApiJob이 잘 launch되는지")
    void t1() throws Exception {
        when(fetchApiJob.getName()).thenReturn("testJob");

        batchJobLauncher.runJob();

        // 실제 실행은 안 되지만, 호출됐는지는 확인할 수 있음
        verify(jobOperator).start(any(Job.class), any());
    }
}

package com.back.global.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.back.global.springBatch.scheduler.TestScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.back.global.config.SchedulingConfig;

@SpringJUnitConfig(classes = {TestScheduler.class, SchedulingConfig.class})
@ActiveProfiles("scheduler-test")
class TestSchedulerTest {
    @MockitoSpyBean
    private TestScheduler testScheduler;

    @Test
    @DisplayName("스케쥴러 테스트 : 3.5초 동안 최소 3번 로그가 찍혀야 함")
    void t1() throws InterruptedException {
        Thread.sleep(3500);
        verify(testScheduler, atLeast(3)).testScheduler();
    }
}

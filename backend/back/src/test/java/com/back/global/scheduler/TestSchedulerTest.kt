package com.back.global.scheduler

import com.back.global.config.SchedulingConfig
import com.back.global.springBatch.scheduler.TestScheduler
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.verify
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(classes = [TestScheduler::class, SchedulingConfig::class])
@ActiveProfiles("scheduler-test")
internal class TestSchedulerTest {

    @MockitoSpyBean
    private lateinit var testScheduler: TestScheduler

    @Test
    @DisplayName("스케쥴러 테스트 : 3.5초 동안 최소 3번 로그가 찍혀야 함")
    fun t1() {
        // 3.5초 대기 (fixedDelay = 1000 설정에 따라 약 3번 이상의 실행을 기다림)
        Thread.sleep(3500)

        // Mockito verify 호출 시 !! 없이 깔끔하게 검증
        verify(testScheduler, atLeast(3)).testScheduler()
    }
}

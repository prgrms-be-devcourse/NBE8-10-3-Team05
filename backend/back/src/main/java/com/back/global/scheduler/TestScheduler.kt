package com.back.global.springBatch.scheduler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("scheduler-test")
class TestScheduler {

    @Scheduled(fixedDelay = 1000)
    fun testScheduler() {
        log.debug("TestScheduler : testScheduler 실행")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TestScheduler::class.java)
    }
}

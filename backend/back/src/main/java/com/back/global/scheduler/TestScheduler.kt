package com.back.global.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Profile("scheduler-test")
@Slf4j
public class TestScheduler {
    @Scheduled(fixedDelay = 1000)
    public void testScheduler() {
        log.debug("TestScheduler : testScheduler 실행");
    }
}

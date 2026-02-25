package com.back.global.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService;
import com.back.global.springBatch.BatchJobLauncher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SyncScheduler {

    private final LawyerCrawlerService lawyerCrawlerService;
    private final BatchJobLauncher batchJobLauncher;

    @Scheduled(cron = "0 30 09 * * *")
    public void runDailyCrawling() {
        // batchJobLauncher.testRunJob();
        log.debug("SyncScheduler : runDailyCrawling 실행");
        log.info("SyncScheduler : 노무사 정보 크롤링(매일)");
        // lawyerCrawlerService.crawlAllPages();
    }

    @Scheduled(cron = "0 30 09 1 * *")
    public void runMonthlyCrawling() {

        log.debug("SyncScheduler : runMonthlyCrawling 실행");
        log.info("SyncScheduler : 노무사 정보 크롤링(매달)");
        // lawyerCrawlerService.crawlAllPages();
    }
}

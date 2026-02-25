package com.back.global.springBatch.scheduler

import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService
import com.back.global.springBatch.BatchJobLauncher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncScheduler(
    private val lawyerCrawlerService: LawyerCrawlerService,
    private val batchJobLauncher: BatchJobLauncher
) {

    @Scheduled(cron = "0 30 09 * * *")
    fun runDailyCrawling() {
        // batchJobLauncher.testRunJob();
        log.debug("SyncScheduler : runDailyCrawling 실행")
        log.info("SyncScheduler : 노무사 정보 크롤링(매일)")
        // lawyerCrawlerService.crawlAllPages();
    }

    @Scheduled(cron = "0 30 09 1 * *")
    fun runMonthlyCrawling() {
        log.debug("SyncScheduler : runMonthlyCrawling 실행")
        log.info("SyncScheduler : 노무사 정보 크롤링(매달)")
        // lawyerCrawlerService.crawlAllPages();
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncScheduler::class.java)
    }
}

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

    // 1. 매일 새벽 02:00 - 정책(Policy) 및 부동산(Estate) 수집
    @Scheduled(cron = "0 0 2 * * *")
    fun runDailyJobs() {
        log.info("Schedule: 매일 정기 수집 Job 실행 (Policy, Estate)")
        batchJobLauncher.runPolicyJob()
        batchJobLauncher.runEstateJob()
    }

    // 2. 매달 1일 새벽 04:00 - 노무사(Lawyer) 수집
    @Scheduled(cron = "0 0 4 1 * *")
    fun runMonthlyJobs() {
        log.info("Schedule: 매달 정기 수집 Job 실행 (Lawyer)")
        batchJobLauncher.runLawyerJob()
    }

    // 3. 6개월 주기 (1월, 7월 1일) 새벽 05:00 - 센터 수집 및 정책 정리
    @Scheduled(cron = "0 0 5 1 1,7 *")
    fun runSemiAnnualJobs() {
        log.info("Schedule: 6개월 주기 정기 Job 실행 (Center, PolicyCleanup)")
        batchJobLauncher.runCenterJob()
        batchJobLauncher.runPolicyCleanupJob()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncScheduler::class.java)
    }
}

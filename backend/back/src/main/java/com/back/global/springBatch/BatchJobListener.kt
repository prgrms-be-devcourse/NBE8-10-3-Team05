package com.back.global.springBatch

import com.back.domain.welfare.estate.entity.EstateRegionCache
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class BatchJobListener : JobExecutionListener {
    private val regionCache: EstateRegionCache? = null

    override fun afterJob(jobExecution: JobExecution) {
        // 종료 시간에서 시작 시간을 빼서 계산
        val start = jobExecution.startTime
        val end = jobExecution.endTime

        if (start != null && end != null) {
            val duration = Duration.between(start, end).toMillis()
            log.info(">>> [Job ID: {}] 최종 완료", jobExecution.jobInstance.jobName)
            log.info(">>> 소요 시간: {}ms (약 {}초)", duration, duration / 1000.0)
            log.info(">>> 최종 상태: {}", jobExecution.status)
        }

        if (jobExecution.status == BatchStatus.FAILED) {
            log.error("=== ❌ 실패 리포트 시작 ===")

            for (stepExecution in jobExecution.stepExecutions) {
                if (stepExecution.status == BatchStatus.FAILED) {
                    // 어떤 지역(Step)에서 문제가 생겼는지 출력
                    log.error("실패한 Step: {}", stepExecution.stepName)

                    // 실제 에러 원인(Exception) 출력
                    log.error("에러 메시지: {}", stepExecution.failureExceptions)
                }
            }
            log.error("========================")
        }

        regionCache!!.init()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BatchJobListener::class.java)
    }
}

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
class BatchJobListener(
    // 생성자 주입을 통해 실제 객체를 받아오도록 수정
    private val regionCache: EstateRegionCache
) : JobExecutionListener {

    override fun afterJob(jobExecution: JobExecution) {
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
                    log.error("실패한 Step: {}", stepExecution.stepName)
                    log.error("에러 메시지: {}", stepExecution.failureExceptions)
                }
            }
            log.error("========================")
        }

        // 이제 regionCache는 null이 아니므로 안전하게 호출 가능합니다.
        // !! 대신 안전하게 호출하거나 주입받은 상태 그대로 사용하세요.
        regionCache.init()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BatchJobListener::class.java)
    }
}

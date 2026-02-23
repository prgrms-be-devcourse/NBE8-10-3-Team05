package com.back.global.springBatch;

import java.time.LocalDateTime;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import com.back.domain.welfare.estate.entity.EstateRegionCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobListener implements JobExecutionListener {
    private EstateRegionCache regionCache;

    @Override
    public void afterJob(JobExecution jobExecution) {
        // 종료 시간에서 시작 시간을 빼서 계산
        LocalDateTime start = jobExecution.getStartTime();
        LocalDateTime end = jobExecution.getEndTime();

        if (start != null && end != null) {
            long duration = java.time.Duration.between(start, end).toMillis();
            log.info(">>> [Job ID: {}] 최종 완료", jobExecution.getJobInstance().getJobName());
            log.info(">>> 소요 시간: {}ms (약 {}초)", duration, duration / 1000.0);
            log.info(">>> 최종 상태: {}", jobExecution.getStatus());
        }

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("=== ❌ 실패 리포트 시작 ===");

            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                if (stepExecution.getStatus() == BatchStatus.FAILED) {
                    // 어떤 지역(Step)에서 문제가 생겼는지 출력
                    log.error("실패한 Step: {}", stepExecution.getStepName());

                    // 실제 에러 원인(Exception) 출력
                    log.error("에러 메시지: {}", stepExecution.getFailureExceptions());
                }
            }
            log.error("========================");
        }

        regionCache.init();
    }
}

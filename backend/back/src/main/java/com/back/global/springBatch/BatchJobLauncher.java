package com.back.global.springBatch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobLauncher {

    private final JobOperator jobOperator; // 권장 실행 방식
    private final Job fetchApiJob;
    private final Job fetchLawyerJob;

    @Async
    public void runJob() {
        try {
            log.info("배치[1/2] 실행 시작: JobName={}, time={}", fetchApiJob.getName(), System.currentTimeMillis());

            JobExecution jobExecution = jobOperator.start(
                    fetchApiJob,
                    new JobParametersBuilder()
                            .addString("job:", fetchApiJob.getName())
                            .addLong("time", System.currentTimeMillis()) // 매번 유니크하게 실행
                            .toJobParameters());

            log.info("배치 실행 완료: JobExecutionId={}, 상태={}", jobExecution.getId(), jobExecution.getStatus());

            log.info("배치[2/2] 실행 시작: JobName={}, time={}", fetchLawyerJob.getName(), System.currentTimeMillis());

            JobExecution jobExecution2 = jobOperator.start(
                    fetchLawyerJob,
                    new JobParametersBuilder()
                            .addString("job:", fetchLawyerJob.getName())
                            .addLong("time", System.currentTimeMillis()) // 매번 유니크하게 실행
                            .toJobParameters());

            log.info("배치 실행 완료: JobExecutionId={}, 상태={}", jobExecution2.getId(), jobExecution2.getStatus());

        } catch (InvalidJobParametersException e) {
            log.error("파라미터가 유효하지 않음: {}", e.getMessage(), e);

        } catch (JobExecutionAlreadyRunningException e) {
            log.error("이미 실행 중인 Job이 있음: {}", e.getMessage(), e);

        } catch (JobRestartException e) {
            log.error("재시작 불가 또는 오류: {}", e.getMessage(), e);

        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("이미 성공적으로 완료된 인스턴스: {}", e.getMessage(), e);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 입력(예: null Job 또는 parameters): {}", e.getMessage(), e);

        } catch (Exception ex) {
            log.error("배치 실행 중 알 수 없는 예외 발생: {}", ex.getMessage(), ex);
        }
    }

    public void testRunJob() {
        log.debug("배치 실행 예시");
    }
}

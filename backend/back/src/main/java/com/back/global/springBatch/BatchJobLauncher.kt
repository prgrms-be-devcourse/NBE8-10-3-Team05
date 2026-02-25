package com.back.global.springBatch

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.InvalidJobParametersException
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.launch.JobRestartException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class BatchJobLauncher(
    private val jobOperator: JobOperator, private val fetchApiJob: Job, private val fetchLawyerJob: Job
) {
    @Async
    fun runJob() {
        try {
            log.info("배치[1/2] 실행 시작: JobName={}, time={}", fetchApiJob.name, System.currentTimeMillis())

            val jobExecution = jobOperator.start(
                fetchApiJob,
                JobParametersBuilder()
                    .addString("job:", fetchApiJob.name)
                    .addLong("time", System.currentTimeMillis()) // 매번 유니크하게 실행
                    .toJobParameters()
            )

            log.info("배치 실행 완료: JobExecutionId={}, 상태={}", jobExecution.id, jobExecution.status)

            log.info("배치[2/2] 실행 시작: JobName={}, time={}", fetchLawyerJob.name, System.currentTimeMillis())

            val jobExecution2 = jobOperator.start(
                fetchLawyerJob,
                JobParametersBuilder()
                    .addString("job:", fetchLawyerJob.name)
                    .addLong("time", System.currentTimeMillis()) // 매번 유니크하게 실행
                    .toJobParameters()
            )

            log.info("배치 실행 완료: JobExecutionId={}, 상태={}", jobExecution2.id, jobExecution2.status)
        } catch (e: InvalidJobParametersException) {
            log.error("파라미터가 유효하지 않음: {}", e.message, e)
        } catch (e: JobExecutionAlreadyRunningException) {
            log.error("이미 실행 중인 Job이 있음: {}", e.message, e)
        } catch (e: JobRestartException) {
            log.error("재시작 불가 또는 오류: {}", e.message, e)
        } catch (e: JobInstanceAlreadyCompleteException) {
            log.error("이미 성공적으로 완료된 인스턴스: {}", e.message, e)
        } catch (e: IllegalArgumentException) {
            log.error("잘못된 입력(예: null Job 또는 parameters): {}", e.message, e)
        } catch (ex: Exception) {
            log.error("배치 실행 중 알 수 없는 예외 발생: {}", ex.message, ex)
        }
    }

    fun testRunJob() {
        log.debug("배치 실행 예시")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BatchJobLauncher::class.java)
    }
}

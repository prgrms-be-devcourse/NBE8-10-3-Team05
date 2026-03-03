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
    private val jobOperator: JobOperator,
    private val fetchApiJob: Job,
    private val fetchCenterJob: Job,
    private val fetchPolicyJob: Job,
    private val fetchEstateJob: Job,
    private val fetchLawyerJob: Job,
    private val policyCleanupJob: Job
) {

    // 매일 도는 수집 Job
    @Async
    fun runPolicyJob() {
        execute(fetchPolicyJob)
    }

    // 6개월마다 도는 정리 Job
    @Async
    fun runPolicyCleanupJob() {
        execute(policyCleanupJob)
    }

    // 매일 도는 수집 Job
    @Async
    fun runEstateJob() {
        execute(fetchEstateJob)
    }

    // 1개월마다 도는 수집 Job
    @Async
    fun runLawyerJob() {
        execute(fetchLawyerJob)
    }

    // 6개월마다 도는 수집 Job
    @Async
    fun runCenterJob() {
        execute(fetchCenterJob)
    }

    @Async
    fun execute(job: Job) {
        try {
            log.info("배치 실행 시작: JobName={}, time={}", job.name, System.currentTimeMillis())

            val jobExecution = jobOperator.start(
                job,
                JobParametersBuilder()
                    .addString("job:", job.name)
                    .addLong("time", System.currentTimeMillis()) // 매번 유니크하게 실행
                    .toJobParameters()
            )

            log.info("배치 실행 완료: JobExecutionId={}, 상태={}", jobExecution.id, jobExecution.status)

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

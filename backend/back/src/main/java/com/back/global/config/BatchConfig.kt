package com.back.global.config

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import com.back.domain.welfare.center.center.entity.Center
import com.back.domain.welfare.center.lawyer.entity.Lawyer
import com.back.domain.welfare.estate.dto.EstateDto
import com.back.domain.welfare.estate.entity.Estate
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.global.springBatch.BatchJobListener
import com.back.global.springBatch.BatchStepCrawlFactory
import com.back.global.springBatch.BatchStepFactory
import com.back.global.springBatch.center.CenterApiItemProcessor
import com.back.global.springBatch.center.CenterApiItemReader
import com.back.global.springBatch.estate.EstateApiItemProcessor
import com.back.global.springBatch.estate.EstateApiItemReader
import com.back.global.springBatch.lawyer.LawyerApiItemReader
import com.back.global.springBatch.policy.PolicyApiItemProcessor
import com.back.global.springBatch.policy.PolicyApiItemReader
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.item.database.JpaItemWriter
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry

@Configuration
@EnableRetry
class BatchConfig(
    private val batchJobListener: BatchJobListener,
    private val batchStepFactory: BatchStepFactory,
    private val batchStepCrawlFactory: BatchStepCrawlFactory,
    private val centerApiItemReader: CenterApiItemReader,
    private val centerApiItemProcessor: CenterApiItemProcessor,
    private val centerJpaItemWriter: JpaItemWriter<Center>,
    private val estateApiItemReader: EstateApiItemReader,
    private val estateApiItemProcessor: EstateApiItemProcessor,
    private val estateJpaItemWriter: JpaItemWriter<Estate>,
    private val policyApiItemReader: PolicyApiItemReader,
    private val policyApiItemProcessor: PolicyApiItemProcessor,
    private val compositeItemWriter: CompositeItemWriter<Policy>,
    private val lawyerApiItemReader: LawyerApiItemReader,
    private val lawyerJpaItemWriter: JpaItemWriter<Lawyer> // Lawyer?에서 Lawyer로 수정 (Type Bound 대응)
) {

    // ==========================================
    // Job Configurations
    // ==========================================
    @Bean
    fun fetchApiJob(
        jobRepository: JobRepository,
        fetchCenterApiStep: Step,
        fetchEstateApiStep: Step,
        fetchPolicyApiStep: Step
    ): Job {
        return JobBuilder("fetchApiJob", jobRepository)
            .listener(batchJobListener)
            .start(fetchCenterApiStep)
            .next(fetchEstateApiStep)
            .next(fetchPolicyApiStep)
            .build()
    }

    @Bean
    fun fetchCenterJob(
        jobRepository: JobRepository,
        fetchCenterApiStep: Step,
    ): Job {
        return JobBuilder("fetchCenterJob", jobRepository)
            .listener(batchJobListener)
            .start(fetchCenterApiStep)
            .build()
    }

    @Bean
    fun fetchEstateJob(
        jobRepository: JobRepository,
        fetchEstateApiStep: Step,
    ): Job {
        return JobBuilder("fetchEstateJob", jobRepository)
            .listener(batchJobListener)
            .start(fetchEstateApiStep)
            .build()
    }

    @Bean
    fun fetchPolicyJob(
        jobRepository: JobRepository,
        fetchPolicyApiStep: Step,
    ): Job {
        return JobBuilder("fetchPolicyJob", jobRepository)
            .listener(batchJobListener)
            .start(fetchPolicyApiStep)
            .build()
    }

    @Bean
    fun policyCleanupJob(
        jobRepository: JobRepository,
        policyCleanupStep: Step,
    ): Job {
        return JobBuilder("policyCleanupJob", jobRepository)
            .listener(batchJobListener)
            .start(policyCleanupStep)
            .build()
    }

    @Bean
    fun fetchLawyerJob(jobRepository: JobRepository): Job {
        val regions = listOf(
            "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
        )

        val builder = JobBuilder("fetchLawyerJob", jobRepository)

        // 첫 번째 지역으로 Job 시작
        var jobFlow = builder.start(createCrawlingStep(regions[0]))

        // 나머지 지역들 연결
        for (i in 1 until regions.size) {
            jobFlow = jobFlow.next(createCrawlingStep(regions[i]))
        }

        return jobFlow.listener(batchJobListener).build()
    }


    // ==========================================
    // Step Configurations
    // ==========================================

    @Bean
    fun fetchCenterApiStep(): Step {
        return batchStepFactory.createApiStep<CenterDto, Center>(
            "fetchCenterApiStep", centerApiItemReader, centerApiItemProcessor, centerJpaItemWriter
        )
    }

    @Bean
    fun fetchEstateApiStep(): Step {
        return batchStepFactory.createApiStep<EstateDto, Estate>(
            "fetchEstateApiStep", estateApiItemReader, estateApiItemProcessor, estateJpaItemWriter
        )
    }

    @Bean
    fun fetchPolicyApiStep(): Step {
        return batchStepFactory.createApiStep<PolicyItem, Policy>(
            "fetchPolicyApiStep", policyApiItemReader, policyApiItemProcessor, compositeItemWriter
        )
    }

    private fun createCrawlingStep(region: String): Step {
        // LawyerApiItemWriter에서 발생했던 제네릭 에러 방지를 위해 Lawyer 타입 명시
        return batchStepCrawlFactory.createCrawlStep<Lawyer, Lawyer>(
            region,
            lawyerApiItemReader,
            lawyerJpaItemWriter
        )
    }

    // ==========================================
    // Cleanup Step Configurations
    // ==========================================

    @Bean
    fun policyCleanupStep(
        batchStepFactory: BatchStepFactory,
        // policyRepository: PolicyRepository // 실제 삭제 로직을 위해 주입 필요
    ): Step {
        return batchStepFactory.createTaskletStep(
            "policyCleanupStep",
            Tasklet { contribution, chunkContext ->
                println("6개월 지난 데이터 청소 중...")

                // 실제 삭제 로직 예시
                // val cutoffDate = LocalDateTime.now().minusMonths(6)
                // val deletedCount = policyRepository.deleteByModifiedDateBefore(cutoffDate)
                // println("$deletedCount 건의 오래된 정책이 삭제되었습니다.")

                RepeatStatus.FINISHED
            }
        )
    }

    @Bean
    fun estateCleanupStep(
        batchStepFactory: BatchStepFactory,
        estateRepository: EstateRepository
    ): Step {
        return batchStepFactory.createTaskletStep("estateCleanupStep") { _, _ ->
            // 오늘 수집되지 않은(즉, API 목록에서 사라진) 부동산 데이터 삭제
            val startOfToday = LocalDateTime.now().with(LocalTime.MIN)
            val deleted = estateRepository.deleteByModifiedDateBefore(startOfToday)

            println("[Estate Cleanup] 이번 배치에서 제외된 데이터 $deleted 건 삭제 완료")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun centerCleanupStep(
        batchStepFactory: BatchStepFactory,
        centerRepository: CenterRepository
    ): Step {
        return batchStepFactory.createTaskletStep("centerCleanupStep") { _, _ ->
            // 이번 6개월 주기 크롤링에 포함 안 된 센터 삭제
            val startOfBatch = LocalDateTime.now().minusHours(1) // 배치 시작 전 시간 기준
            val deleted = centerRepository.deleteByModifiedDateBefore(startOfBatch)

            println("[Center Cleanup] 더 이상 유효하지 않은 센터 $deleted 건 삭제 완료")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun lawyerCleanupStep(
        batchStepFactory: BatchStepFactory,
        lawyerRepository: LawyerRepository
    ): Step {
        return batchStepFactory.createTaskletStep("lawyerCleanupStep") { _, _ ->
            // 이번 크롤링 목록에서 빠진 변호사 삭제
            val startOfBatch = LocalDateTime.now().minusHours(1)
            val deleted = lawyerRepository.deleteByModifiedDateBefore(startOfBatch)

            println("[Lawyer Cleanup] 퇴직 또는 정보 삭제된 변호사 $deleted 건 삭제 완료")
            RepeatStatus.FINISHED
        }
    }


}

package com.back.global.springBatch

import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchStepCrawlFactory(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val crawlingTaskExecutor: AsyncTaskExecutor
) {
    fun <I :Any, O :Any> createCrawlStep(
        region: String?, reader: ItemReader<I>, writer: ItemWriter<O>
    ): Step {
        return StepBuilder("fetchLawyerStep_" + region, jobRepository)
            .chunk<I, O>(8)
            .reader(reader)
            .writer(writer)
            .listener(
                object : StepExecutionListener {
                    // 2. Step 리스너 등록
                    override fun beforeStep(stepExecution: StepExecution) {
                        // 각 Step마다 다른 region 정보를 주입
                        stepExecution.getExecutionContext().put("region", region)
                    }
                })
            .transactionManager(transactionManager)
            .taskExecutor(crawlingTaskExecutor)
            .build()
    }
}

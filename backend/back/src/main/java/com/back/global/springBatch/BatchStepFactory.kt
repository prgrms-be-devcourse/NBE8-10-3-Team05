package com.back.global.springBatch

import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.context.annotation.Configuration
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchStepFactory(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val taskExecutor: AsyncTaskExecutor,
    private val retryPolicy: RetryPolicy
) {
    fun <I :Any, O : Any> createApiStep(
        stepName: String, reader: ItemReader<I>, processor: ItemProcessor<I, O>, writer: ItemWriter<O>
    ): Step {
        return StepBuilder(stepName, jobRepository)
            .chunk<I, O>(1000)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .transactionManager(transactionManager)
            .faultTolerant()
            .retryPolicy(retryPolicy) // 공통으로 사용하는 RetryPolicy
            .taskExecutor(taskExecutor)
            .build()
    }
}

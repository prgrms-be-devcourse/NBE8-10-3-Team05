package com.back.global.springBatch;

import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchStepCrawlFactory {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AsyncTaskExecutor crawlingTaskExecutor;

    public <I, O> Step createCrawlStep(
            String region, ItemReader<I> reader, ItemProcessor<I, O> processor, ItemWriter<O> writer) {

        return new StepBuilder("fetchLawyerStep_" + region, jobRepository)
                .<I, O>chunk(8)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(
                        new StepExecutionListener() { // 2. Step 리스너 등록
                            @Override
                            public void beforeStep(StepExecution stepExecution) {
                                // 각 Step마다 다른 region 정보를 주입
                                stepExecution.getExecutionContext().put("region", region);
                            }
                        })
                .transactionManager(transactionManager)
                .taskExecutor(crawlingTaskExecutor)
                .build();
    }
}

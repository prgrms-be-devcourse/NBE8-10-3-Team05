package com.back.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.domain.welfare.center.lawyer.entity.Lawyer;
import com.back.domain.welfare.estate.dto.EstateDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.global.springBatch.BatchJobListener;
import com.back.global.springBatch.BatchStepCrawlFactory;
import com.back.global.springBatch.BatchStepFactory;
import com.back.global.springBatch.center.CenterApiItemProcessor;
import com.back.global.springBatch.center.CenterApiItemReader;
import com.back.global.springBatch.estate.EstateApiItemProcessor;
import com.back.global.springBatch.estate.EstateApiItemReader;
import com.back.global.springBatch.lawyer.LawyerApiItemReader;
import com.back.global.springBatch.policy.PolicyApiItemProcessor;
import com.back.global.springBatch.policy.PolicyApiItemReader;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableRetry
@RequiredArgsConstructor
public class BatchConfig {
    private final BatchJobListener batchJobListener;
    private final BatchStepFactory batchStepFactory;
    private final BatchStepCrawlFactory batchStepCrawlFactory;

    private final CenterApiItemReader centerApiItemReader;
    private final CenterApiItemProcessor centerApiItemProcessor;
    private final JpaItemWriter<Center> centerJpaItemWriter;

    private final EstateApiItemReader estateApiItemReader;
    private final EstateApiItemProcessor estateApiItemProcessor;
    private final JpaItemWriter<Estate> estateJpaItemWriter;

    private final PolicyApiItemReader policyApiItemReader;
    private final PolicyApiItemProcessor policyApiItemProcessor;
    // private final JpaItemWriter<Policy> policyJpaItemWriter;
    private final CompositeItemWriter<Policy> compositeItemWriter;

    private final LawyerApiItemReader lawyerApiItemReader;
    private final JpaItemWriter<Lawyer> lawyerJpaItemWriter;

    @Bean
    public Job fetchApiJob(
            JobRepository jobRepository, Step fetchCenterApiStep, Step fetchEstateApiStep, Step fetchPolicyApiStep) {

        return new JobBuilder("fetchApiJob", jobRepository)
                .listener(batchJobListener)
                .start(fetchCenterApiStep)
                .next(fetchEstateApiStep)
                .next(fetchPolicyApiStep)
                .build();
    }

    @Bean
    public Step fetchCenterApiStep(BatchStepFactory factory) {
        return factory.<CenterApiResponseDto.CenterDto, Center>createApiStep(
                "fetchCenterApiStep", centerApiItemReader, centerApiItemProcessor, centerJpaItemWriter);
    }

    @Bean
    public Step fetchEstateApiStep(BatchStepFactory factory) {
        return factory.<EstateDto, Estate>createApiStep(
                "fetchEstateApiStep", estateApiItemReader, estateApiItemProcessor, estateJpaItemWriter);
    }

    @Bean
    public Step fetchPolicyApiStep(BatchStepFactory factory) {
        return factory.<PolicyFetchResponseDto.PolicyItem, Policy>createApiStep(
                "fetchPolicyApiStep", policyApiItemReader, policyApiItemProcessor, compositeItemWriter);
    }

    @Bean
    public Job fetchLawyerJob(JobRepository jobRepository, BatchStepCrawlFactory factory) {
        List<String> regions = Arrays.asList(
                "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주");

        SimpleJobBuilder builder = new JobBuilder("fetchLawyerJob", jobRepository)
                .start(createCrawlingStep(factory, regions.get(0))); // 첫 지역 시작

        for (int i = 1; i < regions.size(); i++) {
            builder.next(createCrawlingStep(factory, regions.get(i))); // 다음 지역들 연결
        }

        return builder.listener(batchJobListener).build();
    }

    private Step createCrawlingStep(BatchStepCrawlFactory factory, String region) {
        // Step 이름에 지역명을 넣어 구분 (중요!)
        return factory.createCrawlStep(
                region,
                lawyerApiItemReader, // @StepScope가 동작하여 런타임에 region 주입됨
                null,
                lawyerJpaItemWriter);
    }
}

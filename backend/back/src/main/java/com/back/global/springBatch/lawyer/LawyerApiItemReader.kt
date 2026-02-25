package com.back.global.springBatch.lawyer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService;

import lombok.extern.slf4j.Slf4j;

@Component // Spring이 관리하도록 등록
@StepScope
@Slf4j
public class LawyerApiItemReader extends AbstractPagingItemReader<Lawyer> {

    private final LawyerCrawlerService lawyerCrawlerService;

    @Value("#{stepExecutionContext['region']}")
    String region;

    public LawyerApiItemReader(LawyerCrawlerService apiService) {
        this.lawyerCrawlerService = apiService;
        setPageSize(8); // 한번에 읽을 양 설정
    }

    @Override
    protected void doReadPage() {
        // 결과 저장소 초기화 : Thread-safe한 리스트
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();

        try {
            List<Lawyer> data = lawyerCrawlerService.crawlEachPage(region, getPage() + 1);
            results.addAll(data);

        } catch (ResourceAccessException e) {
            log.error(">>> [네트워크 타임아웃/연결오류] 지역: {}, 페이지: {}, 사유: {}", region, getPage(), e.getMessage());
            throw e; // Step 설정에서 Retry를 설정했다면 다시 시도하게 됨
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn(">>> [차단 감지] 너무 많은 요청을 보냈습니다. (429 Too Many Requests)");
            }
            log.error(">>> [HTTP 에러] 상태코드: {}, 내용: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (RuntimeException e) {
            log.error(">>> [데이터 처리 오류] API 결과 해석 중 에러 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(">>> [알 수 없는 치명적 에러] 지역: {}, 에러: {}", region, e.getClass().getSimpleName());
            throw e;
        }
    }
}

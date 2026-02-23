package com.back.global.springBatch.policy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.service.PolicyApiClient;

import lombok.extern.slf4j.Slf4j;

@Component // Spring이 관리하도록 등록
@Slf4j
public class PolicyApiItemReader extends AbstractPagingItemReader<PolicyFetchResponseDto.PolicyItem> {

    private final PolicyApiClient policyApiClient;
    private final int totalCount;
    private List<String> apiKeys = List.of("1", "2", "3");
    private Integer currentKeyIdx = 0;

    // 생성자를 통해 totalCount를 주입받음
    public PolicyApiItemReader(PolicyApiClient apiService) {
        this.policyApiClient = apiService;
        this.totalCount = 2000;
        setPageSize(1000); // 한번에 읽을 양 설정
    }

    @Override
    protected void doReadPage() {
        // 결과 저장소 초기화 : Thread-safe한 리스트
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();

        // 읽어야 할 데이터가 totalCount를 넘으면 종료
        if (getPage() * getPageSize() >= totalCount) return;

        boolean success = false;
        while (!success && currentKeyIdx < apiKeys.size()) {
            try {
                // 현재 선택된 키로 API 호출 (페이지 번호 전달)

                PolicyFetchRequestDto requestDto = new PolicyFetchRequestDto("", "", "", "");
                PolicyFetchResponseDto responseDto =
                        policyApiClient.fetchPolicyPage(requestDto, getPage() + 1, getPageSize());

                List<PolicyFetchResponseDto.PolicyItem> data =
                        responseDto.result().youthPolicyList();
                results.addAll(data);
                success = true;

            } catch (HttpClientErrorException e) { // 4xx 에러
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) { // 429 에러
                    log.warn("API 한도 초과! 키를 교체합니다.");
                    switchApiKey();
                } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) { // 401 에러
                    log.error("잘못된 API 키입니다. 다음 키로 넘어갑니다.");
                    switchApiKey();
                } else {
                    log.error("클라이언트 에러 발생: {}", e.getMessage());
                    throw e; // 그 외 400, 404 등은 그냥 에러 발생
                }
            } catch (HttpServerErrorException e) { // 5xx 에러
                log.error("API 서버 내부 에러: {}", e.getMessage());
                throw e; // 서버 에러는 키 교체로 해결되지 않으므로 throw
            } catch (Exception e) {
                log.error("알 수 없는 에러: {}", e.getMessage());
                throw e;
            }
        }
    }

    private void switchApiKey() {
        currentKeyIdx++;
        if (currentKeyIdx >= apiKeys.size()) {
            throw new RuntimeException("모든 API 키가 소진되었습니다.");
        }
        log.info("새로운 API 키로 전환: Index {}", currentKeyIdx);
        // 필요하다면 여기서 즉시 재시도하거나, 다음 호출 때 새 키를 쓰도록 설정
    }
}

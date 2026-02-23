package com.back.domain.welfare.center.lawyer.service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LawyerCrawlerService {
    private final String SEARCH_URL = "https://www.youthlabor.co.kr/company/search?text=&area=%s&area2=&page=%d";
    // 시/도인 area1, 페이지 숫자인 page를 넣어서 찾을 수 있도록
    private static final int BATCH_SIZE = 100;
    private final LawyerSaveService lawyerSaveService;
    private final String[] regionList = {
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    };

    public void crawlAllPages() {
        // 시/도 순회
        for (String area1 : regionList) {
            int lastPage = getLastPage(area1);
            crawlMultiPages(area1, 1, lastPage);
        }
        // 크롤링 테스트 용이성을 위해서 crawlMultiPages. getLastPage 따로 분리했음.
    }

    public void crawlMultiPages(String area1, int startPage, int endPage) {
        List<Lawyer> lawyerList = new ArrayList<>();

        for (int i = startPage; i <= endPage; i++) {
            String url = String.format(SEARCH_URL, area1, i);
            Elements rows = crawlAndSelectRows(url);
            // 테이블 내부 각각 행의 정보을 받아옴

            for (Element row : rows) {
                // 열 별로 순회하면서 파싱
                Lawyer lawyer = parseRowToLawyer(row, area1);

                if (lawyer != null) {
                    lawyerList.add(lawyer);
                }

                // 설정한 배치 사이즈 기준마다 저장
                if (lawyerList.size() >= BATCH_SIZE) {
                    lawyerSaveService.saveList(lawyerList);
                    lawyerList.clear();
                }
            }
            applyDelay(300);
        }

        // 마지막 남은 데이터 저장
        if (!lawyerList.isEmpty()) {
            lawyerSaveService.saveList(lawyerList);
            lawyerList.clear();
        }
    }

    @Retryable(
            retryFor = {
                SocketTimeoutException.class,
                ResourceAccessException.class,
                ConnectTimeoutException.class,
                HttpClientErrorException.TooManyRequests.class,
                HttpServerErrorException.ServiceUnavailable.class
            },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000, multiplier = 2.0) // 2초 → 4초 → 8초
            )
    public List<Lawyer> crawlEachPage(String area1, int startPage) {
        List<Lawyer> lawyerList = new ArrayList<>();

        String url = String.format(SEARCH_URL, area1, startPage);
        Elements rows = crawlAndSelectRows(url);

        for (Element row : rows) {
            // 열 별로 순회하면서 파싱
            Lawyer lawyer = parseRowToLawyer(row, area1);

            if (lawyer == null) {
                continue;
            }

            // 법무법인이 없는 노무사는 어차피 연락할 수단도 없기때문에 제거
            if (lawyer.getCorporation() == null || lawyer.getCorporation().isEmpty()) {
                continue;
            }

            lawyerList.add(lawyer);
        }

        return lawyerList;
    }

    // 해당 열의 요소를 분해해서 데이터를 Lawyer로 저장
    private Lawyer parseRowToLawyer(Element row, String area1) {
        Elements cols = row.select("td");

        if (cols.size() < 3) {
            return null;
        }

        String name = cols.get(1).text();
        String corporation = cols.get(2).text();
        String area2 = cols.get(0).text();

        return Lawyer.builder()
                .id(String.format("%s_%s_%s_%s", name, corporation, area1, area2))
                .name(name)
                .corporation(corporation)
                .districtArea1(area1)
                .districtArea2(area2)
                .build();
    }

    // 딜레이 주는 기능 메서드로 따로 분리
    private void applyDelay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Elements crawlAndSelectRows(String url) {
        try {
            Document doc =
                    Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
            return doc.select("table tbody tr");
            // table의 -> tbody -> tr 조회
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // 에러 발생 시 프로그램이 멈추지 않도록, 빈 Elements 반환
            return new Elements();
        }
    }

    // 특정 지역의 마지막 페이지 숫자 받아오기
    public int getLastPage(String area1) {

        try {
            String url = String.format(SEARCH_URL, area1, 1);
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
            Element lastPage = doc.select("a.last").first();
            // 해당 사이트의 HTML의 링크 태그 찾아서, 마지막 페이지 링크 뽑아옴

            if (lastPage != null) {
                String href = lastPage.attr("href");

                String[] href_list = href.split("=");
                String lastPageNum = href_list[href_list.length - 1];
                // url은 https://...&area2=&text=&page=(페이지 숫자) 의 구조로 이루어져 있으니,
                // '=' 기준으로 스플릿해서 배열의 가장 마지막 요소 뽑아옴 -> 마지막 페이지 숫자 나옴.

                return Integer.parseInt(lastPageNum);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return 1;
    }
}

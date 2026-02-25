package com.back.domain.welfare.estate.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EstateApiClientTest {
    @Autowired
    private EstateApiClient estateApiClient;

    @Test
    @DisplayName("실제 API 테스트 : 필요할때만 수동으로 실행")
    @Disabled
    void fetchEstateAPI_real() {
        EstateFetchRequestDto requestDto =
                EstateFetchRequestDto.builder().numOfRows(10).pageNo(1).build();
        EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto);

        assertNotNull(responseDto, "ResponseDto가 null입니다.");
        assertNotNull(responseDto.response, "ResponseDto.response가 null입니다.");
        assertNotNull(responseDto.response.body, "ResponseDto.response.body가 null입니다.");

        EstateFetchResponseDto.Response.BodyDto body = responseDto.response.body;

        assertFalse(body.numOfRows.isBlank(), "numOfRows가 비어있습니다.");
        assertFalse(body.pageNo.isBlank(), "pageNo가 비어있습니다.");
        assertFalse(body.totalCount.isBlank(), "totalCount가 비어있습니다.");
    }
}

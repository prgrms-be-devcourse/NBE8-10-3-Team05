package com.back.domain.welfare.estate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.estate.dto.EstateDto;
import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.repository.EstateRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EstateServiceTest {
    @Autowired
    private EstateService estateService;

    @MockitoBean
    private EstateApiClient estateApiClient;

    @Autowired
    private EstateRepository estateRepository;

    @Test
    @DisplayName("mockResponse 테스트")
    void t0() {
        EstateFetchResponseDto responseDto = mockResponse(10, 9);

        assertNotNull(responseDto, "ResponseDto가 null입니다.");
        assertNotNull(responseDto.response, "ResponseDto.response가 null입니다.");
        assertNotNull(responseDto.response.body, "ResponseDto.response.body가 null입니다.");

        EstateFetchResponseDto.Response.BodyDto body = responseDto.response.body;

        assertFalse(body.numOfRows.isBlank(), "numOfRows가 비어있습니다.");
        assertFalse(body.pageNo.isBlank(), "pageNo가 비어있습니다.");
        assertFalse(body.totalCount.isBlank(), "totalCount가 비어있습니다.");

        assertEquals(9, body.items.size());
    }

    @Test
    @DisplayName("fetchEstatePage 테스트")
    void t1() {
        EstateFetchResponseDto mockRes = mockResponse(10, 9);
        given(estateApiClient.fetchEstatePage(any())).willReturn(mockRes);

        EstateFetchRequestDto requestDto =
                EstateFetchRequestDto.builder().numOfRows(100).pageNo(1).build();
        EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto);

        assertNotNull(responseDto, "ResponseDto가 null입니다.");
        assertNotNull(responseDto.response, "ResponseDto.response가 null입니다.");
        assertNotNull(responseDto.response.body, "ResponseDto.response.body가 null입니다.");

        EstateFetchResponseDto.Response.BodyDto body = responseDto.response.body;

        assertFalse(body.numOfRows.isBlank(), "numOfRows가 비어있습니다.");
        assertFalse(body.pageNo.isBlank(), "pageNo가 비어있습니다.");
        assertFalse(body.totalCount.isBlank(), "totalCount가 비어있습니다.");
        assertNotNull(body.items, "items 리스트 자체가 null입니다.");
    }

    @Test
    @DisplayName("saveEstateList 테스트")
    void t2() {
        EstateFetchResponseDto mockRes = mockResponse(10, 9);
        given(estateApiClient.fetchEstatePage(any())).willReturn(mockRes);

        EstateFetchRequestDto requestDto =
                EstateFetchRequestDto.builder().numOfRows(10).pageNo(1).build();
        EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto);

        List<Estate> estateList = estateService.saveEstateList(responseDto);
        int savedCnt = (int) estateRepository.count();

        assertEquals(estateList.size(), savedCnt);
    }

    @Test
    @DisplayName("fetchEstateList 테스트")
    void t3() {
        EstateFetchResponseDto mockRes = mockResponse(10, 9);
        given(estateApiClient.fetchEstatePage(any())).willReturn(mockRes);

        List<Estate> estateList = estateService.fetchEstateList();
        int savedCount = (int) estateRepository.count();

        assertEquals(9, estateList.size());
        assertEquals(9, savedCount);
    }

    @Test
    @DisplayName("fetchEstateList 테스트 : return값이 200개일 때")
    void t4() {
        EstateFetchResponseDto mockRes1 = mockResponse(200, 100);
        EstateFetchResponseDto mockRes2 = mockResponse(200, 100);
        given(estateApiClient.fetchEstatePage(any())).willReturn(mockRes1).willReturn(mockRes2);

        List<Estate> estateList = estateService.fetchEstateList();

        int totalCnt = 200;
        int savedCount = (int) estateRepository.count();

        verify(estateApiClient, times(2)).fetchEstatePage(any());
        assertEquals(totalCnt, estateList.size());
        assertEquals(savedCount, estateList.size());
    }

    private EstateFetchResponseDto mockResponse(int pageSize, int currCnt) {
        List<EstateDto> mockItems = IntStream.range(0, currCnt)
                .mapToObj(i -> EstateDto.builder().build()) // EstateDto에도 @Builder가 있어야 합니다.
                .toList();

        EstateFetchResponseDto.Response.BodyDto body = new EstateFetchResponseDto.Response.BodyDto(
                "100", // numOfRows
                "1", // pageNo
                String.valueOf(pageSize), // totalCount
                mockItems);

        EstateFetchResponseDto.Response.HeaderDto header =
                new EstateFetchResponseDto.Response.HeaderDto("00", "NORMAL SERVICE.");

        return EstateFetchResponseDto.builder()
                .response(new EstateFetchResponseDto.Response(header, body))
                .build();
    }
}

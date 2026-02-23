package com.back.domain.welfare.center.center.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.domain.welfare.center.center.repository.CenterRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CenterServiceTest {
    @Autowired
    private CenterService centerService;

    @Autowired
    private CenterRepository centerRepository;

    @MockitoBean
    private CenterApiService centerApiService;

    @Test
    @DisplayName("getCenterData 테스트")
    void t1() {
        int totalCnt = 9;
        CenterApiResponseDto responseDto = createResponseData(1, totalCnt, 10, totalCnt);

        Mockito.when(centerApiService.fetchCenter(any())).thenReturn(responseDto);

        List<Center> centerList = centerService.getCenterData();

        long savedCount = centerRepository.count();

        assertThat(centerList).isNotEmpty();
        assertThat(totalCnt).isEqualTo(centerList.size());
        assertThat(totalCnt).isEqualTo(savedCount);
    }

    @Test
    @DisplayName("getCenterData 테스트 : 200개 이상 받을 때")
    void t2() {
        int totalCnt = 250;
        CenterApiResponseDto responseDto = createResponseData(1, totalCnt, 10, totalCnt);

        Mockito.when(centerApiService.fetchCenter(any())).thenReturn(responseDto);

        List<Center> centerList = centerService.getCenterData();

        long savedCount = centerRepository.count();

        assertThat(centerList).isNotEmpty();
        assertThat(totalCnt).isEqualTo(centerList.size());
        assertThat(totalCnt).isEqualTo(savedCount);
    }

    private CenterApiResponseDto createResponseData(int page, int perPage, int total, int currentCount) {
        List<CenterApiResponseDto.CenterDto> mockList = new ArrayList<>();
        for (int i = 1; i <= currentCount; i++) {
            mockList.add(new CenterApiResponseDto.CenterDto(
                    i + ((page - 1) * perPage), "강원", "복지관" + i, "주소", "010-0000-0000", "운영주체", "법인"));
        }
        return new CenterApiResponseDto(page, perPage, total, currentCount, total, mockList);
    }
}

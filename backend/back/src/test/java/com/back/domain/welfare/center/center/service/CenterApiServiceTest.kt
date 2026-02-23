package com.back.domain.welfare.center.center.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto;
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CenterApiServiceTest {
    @Autowired
    private CenterApiService centerApiService;

    @Test
    @DisplayName("실제 API 테스트 : 필요할때만 수동으로 실행")
    @Disabled
    void t1() {
        CenterApiRequestDto centerApiRequestDto = CenterApiRequestDto.from(1, 100);
        CenterApiResponseDto responseDto = centerApiService.fetchCenter(centerApiRequestDto);

        assertNotNull(responseDto);
        assertNotNull(responseDto.data);
        assertFalse(responseDto.data.isEmpty());
        assertTrue(responseDto.data.size() < 101);
    }
}

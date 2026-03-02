package com.back.domain.welfare.center.center.service

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto.Companion.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Disabled
internal class CenterApiServiceTest {
    @Autowired
    private lateinit var centerApiService: CenterApiService

    @Test
    @DisplayName("실제 API 테스트 : 필요할때만 수동으로 실행")
    fun t1() {
        val centerApiRequestDto = from(1, 100)
        val responseDto = centerApiService.fetchCenter(centerApiRequestDto)

        assertThat(responseDto).isNotNull
        assertThat(responseDto.data).isNotEmpty
        assertThat(responseDto.data.size).isLessThanOrEqualTo(100)
    }
}

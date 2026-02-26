package com.back.domain.member.geo.service

import com.back.domain.member.geo.dto.AddressDto
import com.back.domain.member.geo.dto.GeoApiResponseDto
import com.back.domain.member.geo.entity.Address
import com.back.global.exception.ServiceException
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

@Service
class GeoService(private val geoApiService: GeoApiService) {
    fun getGeoCode(addressDto: AddressDto): Address {
        val responseDto = geoApiService.fetchGeoCode(addressDto)

        val updatedDto = responseDto?.documents
            ?.takeIf { it.isNotEmpty() } // 리스트가 비어있지 않은지 확인
            ?.firstOrNull()              // 첫 번째 Document 꺼내기 (안전하게)
            ?.address                    // address 추출
            ?.let { Address.of(addressDto, it) } // Address 엔티티로 변환
            ?: throw ServiceException(   // 위 과정 중 하나라도 null이면 예외 발생
                "501",
                "kakao local api return값에서 parsing에 실패하였습니다."
            )

        return updatedDto
    }
}

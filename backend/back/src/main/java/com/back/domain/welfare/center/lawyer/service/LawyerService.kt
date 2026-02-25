package com.back.domain.welfare.center.lawyer.service

import com.back.domain.welfare.center.lawyer.dto.LawyerReq
import com.back.domain.welfare.center.lawyer.dto.LawyerRes
import com.back.domain.welfare.center.lawyer.repository.LawyerRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LawyerService(private val lawyerRepository: LawyerRepository) {

    @Transactional(readOnly = true)
    @Cacheable(value = ["lawyer"], key = "{#lawyerReq, #pageable}")
    fun searchByDistrict(lawyerReq: LawyerReq, pageable: Pageable): Page<LawyerRes> {
        val area1 = normalizeArea1(lawyerReq.area1)
        // 서울특별시 -> 서울, 전라북도 -> 전북 으로 정규화 위함
        // SidoNormalizer는 서울 -> 서울특별시 로 반환하는 반대 기능이므로 사용하지 않음

        val area2 = lawyerReq.area2 ?: ""
        // 군/구 정보가 null이면 ""을 사용하도록..

        val lawyerPage = lawyerRepository.findByDistrictArea1AndDistrictArea2Containing(area1, area2, pageable)

        return lawyerPage.map(::LawyerRes)
    }

    // 서울특별시 -> 서울, 경상남도 -> 경남과 같이 area1 값을 처리
    private fun normalizeArea1(area1: String): String {
        if (area1.length <= 2) return area1

        // 앞 두 글자 추출 (서울, 경기, 제주, 전라, 경상, 충청..)
        val prefix = area1.take(2)

        // 충청남도 -> 충북처럼 남/북 구분이 필요한 지역 처리
        return when (prefix) {
            "전라" -> if (area1.contains("남")) "전남" else "전북"
            "경상" -> if (area1.contains("남")) "경남" else "경북"
            "충청" -> if (area1.contains("남")) "충남" else "충북"
            else -> prefix
        }
    }
}

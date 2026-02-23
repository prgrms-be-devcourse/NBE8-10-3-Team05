package com.back.domain.welfare.center.lawyer.service

import com.back.domain.welfare.center.lawyer.entity.Lawyer
import com.back.domain.welfare.center.lawyer.repository.LawyerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LawyerSaveService(private val lawyerRepository: LawyerRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun saveList(lawyerList: List<Lawyer>) {
        try {
            // 기존 DB에 없는 데이터만 골라서 한번에 저장
            val newLawyers = lawyerList.filter { lawyer ->
                !lawyerRepository.existsByNameAndCorporation(lawyer.name, lawyer.corporation)
            }

            if (newLawyers.isNotEmpty()) {
                lawyerRepository.saveAll(newLawyers)
            }

        } catch (e: Exception) {
            log.error("노무사 정보 저장 에러", e)
        }
    }
}

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

        val names = lawyerList.map { it.name }.distinct()
        val existingLawyers = lawyerRepository.findByNameIn(names)
        val existingLawyerSet = existingLawyers.map { "${it.name}&${it.corporation}" }.toSet()
        // lawyerList에서 lawyer 이름만 뽑아와서 "이름&법인" 형태로 set에 저장"

        val lawyersToSave = lawyerList.filter { lawyer ->
            val key = "${lawyer.name}&${lawyer.corporation}"
            key !in existingLawyerSet
        }
        // lawyerSet에 속하지 않은 lawyerList 요소만 필터링해 저장
        // -> 루프 돌면서 매번 쿼리를 날려 확인할 필요없이, 최소한의 쿼리로조회로 DB에 없는 노무사 데이터만 저장 가능

        if (lawyersToSave.isNotEmpty()) {
            lawyerRepository.saveAll(lawyersToSave)
            log.info("노무사 데이터 {}건 저장", lawyersToSave.size)
        }
    }
}

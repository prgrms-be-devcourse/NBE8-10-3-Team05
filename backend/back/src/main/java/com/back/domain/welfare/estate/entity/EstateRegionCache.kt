package com.back.domain.welfare.estate.entity

import com.back.domain.welfare.estate.dto.EstateRegionDto
import com.back.domain.welfare.estate.repository.EstateRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

@Component
class EstateRegionCache(private val estateRepository: EstateRepository) {

    val regionList: MutableList<EstateRegionDto> = CopyOnWriteArrayList()

    fun init() {
        regionList.clear()

        // 부모 지역(Level 1) 추가
        val parents = estateRepository.findDistinctBrtcNmBy()
        parents.forEach { p ->
            regionList.add(EstateRegionDto(p, null, 1))
        }

        // 자식 지역(Level 2) 추가
        val children = estateRepository.findDistinctBrtcNmAndSignguNmBy()
        children.forEach { c ->
            val parentName = c?.get(0) as? String
            val childName = c?.get(1) as? String

            if (childName != null) {
                regionList.add(EstateRegionDto(childName, parentName, 2)!!)
            }
        }

        log.info("regionList init 완료 : {}", regionList)
    }

    companion object {
        private val log = LoggerFactory.getLogger(EstateRegionCache::class.java)
    }
}

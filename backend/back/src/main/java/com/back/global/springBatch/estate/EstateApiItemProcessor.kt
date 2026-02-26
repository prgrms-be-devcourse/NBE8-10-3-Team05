package com.back.global.springBatch.estate

import com.back.domain.welfare.estate.dto.EstateDto
import com.back.domain.welfare.estate.entity.Estate
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class EstateApiItemProcessor : ItemProcessor<EstateDto, Estate> {
    override fun process(item: EstateDto): Estate? {
        return Estate(item)
    }
}

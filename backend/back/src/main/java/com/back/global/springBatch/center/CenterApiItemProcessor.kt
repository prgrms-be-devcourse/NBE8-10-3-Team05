package com.back.global.springBatch.center

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import com.back.domain.welfare.center.center.dto.dtoToEntity
import com.back.domain.welfare.center.center.entity.Center
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class CenterApiItemProcessor : ItemProcessor<CenterDto, Center> {
    override fun process(item: CenterDto): Center = item.dtoToEntity()
}

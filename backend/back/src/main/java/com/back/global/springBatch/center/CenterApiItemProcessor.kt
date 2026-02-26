package com.back.global.springBatch.center

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import com.back.domain.welfare.center.center.dto.dtoToEntity
import com.back.domain.welfare.center.center.entity.Center
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class CenterApiItemProcessor : ItemProcessor<CenterDto, Center> {
    @Throws(Exception::class) // Java와의 호환성 및 Checked Exception 허용을 위해 추가
    override fun process(item: CenterDto): Center {
        return try {
            item.dtoToEntity()
        } catch (e: java.net.SocketTimeoutException) {
            // 소켓 예외를 명시적으로 다시 던짐
            throw e
        } catch (e: Exception) {
            // 그 외의 예외 처리
            throw RuntimeException("데이터 변환 중 알 수 없는 에러 발생", e)
        }
    }
}

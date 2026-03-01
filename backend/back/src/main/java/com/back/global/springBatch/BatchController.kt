package com.back.global.springBatch

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class BatchController(private val batchJobLauncher: BatchJobLauncher) {
    @GetMapping("/batchTest/{entity}")
    @ResponseBody
    fun setup(@PathVariable entity: String): String {
        log.info(">>> 사용자 요청: 배치 프로세스 가동")

        // 비동기로 실행 (배치 실행 메서드에 @Async가 붙어있어야 함)
        when (entity.lowercase()) {
            "policy" -> {
                batchJobLauncher.runPolicyJob()
                batchJobLauncher.runPolicyCleanupJob() // 정책은 수집 후 클린업도 함께 테스트
            }
            "estate" -> batchJobLauncher.runEstateJob()
            "lawyer" -> batchJobLauncher.runLawyerJob()
            "center" -> batchJobLauncher.runCenterJob()
            else -> return "알 수 없는 엔티티입니다: $entity"
        }

        // 브라우저에는 즉시 응답 반환
        return "null"
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BatchController::class.java)
    }
}

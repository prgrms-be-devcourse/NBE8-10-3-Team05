package com.back.global.springBatch

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class BatchController(private val batchJobLauncher: BatchJobLauncher) {
    @GetMapping("/batchTest")
    @ResponseBody
    fun setup(): String {
        log.info(">>> 사용자 요청: 배치 프로세스 가동")

        // 비동기로 실행 (배치 실행 메서드에 @Async가 붙어있어야 함)
        batchJobLauncher.runJob()

        // 브라우저에는 즉시 응답 반환
        return "null"
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BatchController::class.java)
    }
}

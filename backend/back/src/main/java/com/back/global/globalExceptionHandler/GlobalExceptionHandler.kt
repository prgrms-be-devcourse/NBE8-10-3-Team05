package com.back.global.globalExceptionHandler

import com.back.global.exception.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException::class)
    fun handle(ex: ServiceException): ResponseEntity<Map<String, Any?>> {
        // 기존 로그 출력 로직 그대로 유지
        log.debug("[{}] : {} , {}", ex.location, ex.resultCode, ex.msg, ex)

        var httpStatus = 500
        val fullCode = ex.resultCode

        try {
            // resultCode의 앞 3자리를 파싱하여 HTTP 상태 코드 결정
            val parsedCode = fullCode.substring(0, 3).toInt()
            if (HttpStatus.resolve(parsedCode) != null) {
                httpStatus = parsedCode
            } else {
                log.warn("정의되지 않은 HTTP 상태 코드: {} (기본값 500 사용하여 에러 나지 않음)", parsedCode)
            }
        } catch (exception: NumberFormatException) {
            log.error("유효하지 않은 에러 코드 형식: {}", fullCode)
        }

        // 특정 키워드 포함 시 상태 코드 보정 로직 유지
        if (httpStatus == 500) {
            if (fullCode.contains("401")) {
                httpStatus = 401
            } else if (fullCode.contains("404")) {
                httpStatus = 404
            } else if (fullCode.contains("409")) {
                httpStatus = 409
            } else if (fullCode.contains("400")) {
                httpStatus = 400
            }
        }

        // 응답 바디 생성 및 반환
        return ResponseEntity.status(httpStatus)
            .body(mapOf("resultCode" to fullCode, "msg" to ex.msg))
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}

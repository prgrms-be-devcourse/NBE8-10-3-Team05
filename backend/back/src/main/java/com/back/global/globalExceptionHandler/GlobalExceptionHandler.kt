package com.back.global.globalExceptionHandler;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handle(ServiceException ex) {
        log.debug("[{}] : {} , {}", ex.getLocation(), ex.getResultCode(), ex.getMsg(), ex);

        int httpStatus = 500;
        String fullCode = ex.getResultCode();

        try {
            int parsedCode = Integer.parseInt(fullCode.substring(0, 3));
            if (HttpStatus.resolve(parsedCode) != null) {
                httpStatus = parsedCode;
            } else {
                log.warn("정의되지 않은 HTTP 상태 코드: {} (기본값 500 사용하여 에러 나지 않음)", parsedCode);
            }
        } catch (NumberFormatException exception) {
            log.error("유효하지 않은 에러 코드 형식: {}", fullCode);
        }

        if (httpStatus == 500) {
            if (fullCode.contains("401")) {
                httpStatus = 401;
            } else if (fullCode.contains("404")) {
                httpStatus = 404;
            } else if (fullCode.contains("409")) {
                httpStatus = 409;
            } else if (fullCode.contains("400")) {
                httpStatus = 400;
            }
        }

        return ResponseEntity.status(httpStatus).body(Map.of("resultCode", fullCode, "msg", ex.getMsg()));
    }
}

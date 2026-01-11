package com.wjc.codetest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 문제 : 전역 예외 처리 설계에 대한 문제
 *  - REST API임에도 불구하고 @ControllerAdvice를 사용하고 있어, 예외 응답이 View 기반 처리와 혼재될 수 있는 구조이다.
 *  - 예외 응답 방식이 명확하지 않아, 모든 Handler 메서드에 @ResponseBody를 반복적으로 선언해야 하는 비효율이 발생한다.
 *
 * 원인 : 코드
 *  - RestAPI 전용 전역 예외 처리에 적합한 @RestControllerAdvice의 역할을 고려하지 않고, @ControllerAdvice를 사용하여 예외 처리 책임이 불명확해졌다.
 *
 * 개선안
 * <p>
 *     1. @ControllerAdvice 대신 @RestControllerAdvice를 사용하여 전역 예외 처리를 RestAPI 전용으로 명확히 분리함으로써,
 *        모든 예외 응답을 일관되게 Body 기반(JSON)으로 반환하고, @ResponseBody 중복 선언을 제거하여 코드 가독성과 유지보수성을 향상시키는 것이 바람직하다고 판단된다.
 * </p>
 */
@Slf4j
@ControllerAdvice(value = {"com.wjc.codetest.product.controller"})
public class GlobalExceptionHandler {

    /**
     * 문제 : 에러 처리에 대한 문제
     *  - 서버 내부 오류가 발생했을 때, 클라이언트에 전달되는 오류 정보가 부족하여 API 사용성과 디버깅 효율이 저하된다.
     *
     * 원인 : 코드
     *  - RuntimeException이 개별 컨트롤러에서 처리되지 않고 그대로 전파되며, 오류 응답에 대한 공통 포맷이 정의되어 있지 않다.
     *
     * 개선안
     * <p>
     *    1. RuntimeException을 전역 예외 처리기로 일괄 처리하여 HTTP 500 상태 코드와 함께 공통 Error Response 포맷을 반환해야 한다,
     *       따라서, 예외 응답에 대한 일관성을 확보하고 클라이언트가 오류를 예측 가능하게 처리할 수 있도록 하는 것이 협업에 있어 불필요한 리소스 낭비를 줄이고 효율적이라고 판단된다.
     * </p>
     */
    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> runTimeException(Exception e) {
        log.error("status :: {}, errorType :: {}, errorCause :: {}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "runtimeException",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

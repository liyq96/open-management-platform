package com.openplatform.common.web.exception;

import com.openplatform.common.core.error.CommonErrorCode;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.common.web.support.RequestIdSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * REST 接口全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception, HttpServletRequest request) {
        String requestId = RequestIdSupport.getRequestId(request);
        log.warn("Business request failed, code={}, requestId={}, message={}",
                exception.getCode(), requestId, exception.getMessage());
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage(), requestId));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            Exception exception, HttpServletRequest request) {
        String requestId = RequestIdSupport.getRequestId(request);
        log.warn("Request validation failed, requestId={}, exceptionType={}",
                requestId, exception.getClass().getSimpleName());
        return ResponseEntity.status(CommonErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.failure(CommonErrorCode.BAD_REQUEST, requestId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        String requestId = RequestIdSupport.getRequestId(request);
        log.error("Unexpected request failure, requestId={}", requestId, exception);
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.failure(CommonErrorCode.INTERNAL_SERVER_ERROR, requestId));
    }
}

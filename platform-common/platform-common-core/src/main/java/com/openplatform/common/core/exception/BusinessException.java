package com.openplatform.common.core.exception;

import com.openplatform.common.core.error.ErrorCode;
import lombok.Getter;

/**
 * 可预期的业务异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    private final int httpStatus;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BusinessException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}

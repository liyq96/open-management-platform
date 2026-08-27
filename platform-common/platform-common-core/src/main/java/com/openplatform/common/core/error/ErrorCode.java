package com.openplatform.common.core.error;

/**
 * 统一错误码定义。
 */
public interface ErrorCode {

    String getCode();

    String getMessage();

    int getHttpStatus();
}

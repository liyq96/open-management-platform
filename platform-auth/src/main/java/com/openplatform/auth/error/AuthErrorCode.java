package com.openplatform.auth.error;

import com.openplatform.common.core.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 认证模块错误码。
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS("AUTH_001", "账号或密码错误", 401),
    ACCOUNT_DISABLED("AUTH_002", "账号已被禁用", 403),
    TOKEN_INVALID("AUTH_003", "Token 无效或已过期", 401),
    CAPTCHA_INVALID("AUTH_004", "验证码错误或已过期", 400);

    private final String code;

    private final String message;

    private final int httpStatus;
}

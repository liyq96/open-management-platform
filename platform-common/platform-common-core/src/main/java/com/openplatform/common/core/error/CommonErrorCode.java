package com.openplatform.common.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用错误码。
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    BAD_REQUEST("COMMON_400", "请求参数错误", 400),
    UNAUTHORIZED("COMMON_401", "未登录或登录状态已失效", 401),
    FORBIDDEN("COMMON_403", "没有访问权限", 403),
    NOT_FOUND("COMMON_404", "请求资源不存在", 404),
    CONFLICT("COMMON_409", "请求数据冲突", 409),
    INTERNAL_SERVER_ERROR("COMMON_500", "系统内部异常", 500);

    private final String code;

    private final String message;

    private final int httpStatus;
}

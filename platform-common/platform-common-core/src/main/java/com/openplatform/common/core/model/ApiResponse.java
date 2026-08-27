package com.openplatform.common.core.model;

import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对外 API 统一响应。
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String code;

    private String message;

    private T data;

    private String requestId;

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(CommonConstants.SUCCESS_CODE, "操作成功", data, requestId);
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String requestId) {
        return failure(errorCode.getCode(), errorCode.getMessage(), requestId);
    }

    public static <T> ApiResponse<T> failure(String code, String message, String requestId) {
        return new ApiResponse<>(code, message, null, requestId);
    }
}

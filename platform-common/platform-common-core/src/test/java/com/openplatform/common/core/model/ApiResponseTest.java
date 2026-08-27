package com.openplatform.common.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openplatform.common.core.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("ok", "request-1");

        assertEquals("200", response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertEquals("ok", response.getData());
        assertEquals("request-1", response.getRequestId());
    }

    @Test
    void shouldCreateFailureResponse() {
        ApiResponse<Void> response = ApiResponse.failure(CommonErrorCode.BAD_REQUEST, "request-2");

        assertEquals("COMMON_400", response.getCode());
        assertEquals("请求参数错误", response.getMessage());
        assertNull(response.getData());
        assertEquals("request-2", response.getRequestId());
    }
}

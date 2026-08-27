package com.openplatform.common.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.error.CommonErrorCode;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.core.model.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBusinessExceptionWithoutExposingStackTrace() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE, "request-1");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(
                new BusinessException(CommonErrorCode.CONFLICT), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("COMMON_409", response.getBody().getCode());
        assertEquals("请求数据冲突", response.getBody().getMessage());
        assertEquals("request-1", response.getBody().getRequestId());
        assertNull(response.getBody().getData());
    }
}

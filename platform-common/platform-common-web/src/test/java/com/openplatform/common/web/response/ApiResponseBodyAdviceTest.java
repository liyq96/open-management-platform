package com.openplatform.common.web.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.model.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiResponseBodyAdviceTest {

    private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

    @Test
    void shouldAddRequestIdToResponse() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE, "request-1");
        ApiResponse<String> body = ApiResponse.success("ok");

        advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(new MockHttpServletResponse()));

        assertEquals("request-1", body.getRequestId());
    }
}

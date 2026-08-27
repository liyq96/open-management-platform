package com.openplatform.common.web.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.openplatform.common.core.constant.CommonConstants;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void shouldReuseValidRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstants.REQUEST_ID_HEADER, "request-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("request-1", response.getHeader(CommonConstants.REQUEST_ID_HEADER));
        assertEquals("request-1", request.getAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE));
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNotNull(response.getHeader(CommonConstants.REQUEST_ID_HEADER));
        assertNotNull(request.getAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE));
    }

    @Test
    void shouldReplaceInvalidRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstants.REQUEST_ID_HEADER, "invalid request id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String requestId = response.getHeader(CommonConstants.REQUEST_ID_HEADER);
        assertNotNull(requestId);
        assertEquals(32, requestId.length());
    }
}

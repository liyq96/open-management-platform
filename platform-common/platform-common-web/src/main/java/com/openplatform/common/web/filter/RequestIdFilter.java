package com.openplatform.common.web.filter;

import com.openplatform.common.core.constant.CommonConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 为每个 HTTP 请求建立可追踪的请求编号。
 */
public class RequestIdFilter extends OncePerRequestFilter implements Ordered {

    private static final String VALID_REQUEST_ID_PATTERN = "[A-Za-z0-9._-]{1,128}";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        request.setAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE, requestId);
        response.setHeader(CommonConstants.REQUEST_ID_HEADER, requestId);
        MDC.put(CommonConstants.REQUEST_ID_MDC_KEY, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CommonConstants.REQUEST_ID_MDC_KEY);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(CommonConstants.REQUEST_ID_HEADER);
        if (StringUtils.hasText(requestId) && requestId.matches(VALID_REQUEST_ID_PATTERN)) {
            return requestId;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}

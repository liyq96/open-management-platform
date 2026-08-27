package com.openplatform.common.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.common.core.security.InternalAccessSigner;
import com.openplatform.common.web.config.InternalAccessProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 拒绝没有有效 Gateway 签名的内部服务请求。
 */
public class InternalAccessFilter extends OncePerRequestFilter implements Ordered {

    private final InternalAccessProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public InternalAccessFilter(
            InternalAccessProperties properties,
            ObjectMapper objectMapper,
            Clock clock) {
        InternalAccessSigner.validateSecret(properties.getSecret());
        if (properties.getMaxClockSkew() == null || properties.getMaxClockSkew().isNegative()
                || properties.getMaxClockSkew().isZero()) {
            throw new IllegalStateException("platform.internal-access.max-clock-skew must be positive");
        }
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!isValid(request)) {
            writeForbidden(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private boolean isValid(HttpServletRequest request) {
        String timestampHeader = request.getHeader(CommonConstants.GATEWAY_TIMESTAMP_HEADER);
        String signature = request.getHeader(CommonConstants.GATEWAY_SIGNATURE_HEADER);
        String requestId = request.getHeader(CommonConstants.REQUEST_ID_HEADER);
        if (timestampHeader == null || signature == null || requestId == null) {
            return false;
        }
        try {
            long timestamp = Long.parseLong(timestampHeader);
            long now = clock.millis();
            long maxClockSkew = properties.getMaxClockSkew().toMillis();
            if (timestamp < now - maxClockSkew || timestamp > now + maxClockSkew) {
                return false;
            }
            String expected = InternalAccessSigner.sign(properties.getSecret(), timestamp, requestId);
            return InternalAccessSigner.matches(expected, signature);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void writeForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String requestId = (String) request.getAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE);
        objectMapper.writeValue(response.getOutputStream(),
                ApiResponse.failure("COMMON_403", "禁止直接访问内部服务，请通过 Gateway 访问", requestId));
    }
}

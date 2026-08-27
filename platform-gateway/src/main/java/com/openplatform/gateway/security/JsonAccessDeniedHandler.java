package com.openplatform.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.core.error.CommonErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关无权限响应。
 */
public class JsonAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final GatewayErrorResponseWriter responseWriter;

    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.responseWriter = new GatewayErrorResponseWriter(objectMapper);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return responseWriter.write(exchange, CommonErrorCode.FORBIDDEN);
    }
}

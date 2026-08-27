package com.openplatform.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.core.error.CommonErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关未认证响应。
 */
public class JsonAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final GatewayErrorResponseWriter responseWriter;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.responseWriter = new GatewayErrorResponseWriter(objectMapper);
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
        return responseWriter.write(exchange, CommonErrorCode.UNAUTHORIZED);
    }
}

package com.openplatform.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.error.ErrorCode;
import com.openplatform.common.core.model.ApiResponse;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关安全错误 JSON 输出。
 */
@RequiredArgsConstructor
public class GatewayErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public Mono<Void> write(ServerWebExchange exchange, ErrorCode errorCode) {
        String requestId = (String) exchange.getAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE);
        ApiResponse<Void> response = ApiResponse.failure(errorCode, requestId);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException exception) {
            bytes = "{\"code\":\"COMMON_500\",\"message\":\"系统内部异常\"}"
                    .getBytes(StandardCharsets.UTF_8);
        }
        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(errorCode.getHttpStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

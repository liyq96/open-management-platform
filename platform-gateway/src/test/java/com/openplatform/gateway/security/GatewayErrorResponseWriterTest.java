package com.openplatform.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.error.CommonErrorCode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class GatewayErrorResponseWriterTest {

    @Test
    void shouldWriteUnauthorizedJsonResponse() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/private"));
        exchange.getAttributes().put(CommonConstants.REQUEST_ID_ATTRIBUTE, "request-1");
        GatewayErrorResponseWriter writer = new GatewayErrorResponseWriter(new ObjectMapper());

        writer.write(exchange, CommonErrorCode.UNAUTHORIZED).block();

        assertEquals(401, exchange.getResponse().getStatusCode().value());
        DataBuffer buffer = exchange.getResponse().getBody().blockFirst();
        String body = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(body.contains("COMMON_401"));
        assertTrue(body.contains("request-1"));
    }
}

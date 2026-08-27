package com.openplatform.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.security.InternalAccessSigner;
import com.openplatform.gateway.config.GatewayInternalAccessProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class GatewayInternalAccessFilterTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final long NOW = 1_700_000_000_000L;

    @Test
    void shouldReplaceExternalHeadersWithGatewaySignature() {
        GatewayInternalAccessProperties properties = new GatewayInternalAccessProperties();
        properties.setEnabled(true);
        properties.setSecret(SECRET);
        GatewayInternalAccessFilter filter = new GatewayInternalAccessFilter(properties,
                Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/auth/login")
                .header(CommonConstants.REQUEST_ID_HEADER, "request-1")
                .header(CommonConstants.GATEWAY_TIMESTAMP_HEADER, "forged")
                .header(CommonConstants.GATEWAY_SIGNATURE_HEADER, "forged"));

        filter.filter(exchange, filteredExchange -> {
            String timestamp = filteredExchange.getRequest().getHeaders()
                    .getFirst(CommonConstants.GATEWAY_TIMESTAMP_HEADER);
            String signature = filteredExchange.getRequest().getHeaders()
                    .getFirst(CommonConstants.GATEWAY_SIGNATURE_HEADER);
            assertEquals(Long.toString(NOW), timestamp);
            assertNotEquals("forged", signature);
            assertTrue(InternalAccessSigner.matches(signature,
                    InternalAccessSigner.sign(SECRET, NOW, "request-1")));
            return Mono.empty();
        }).block();
    }
}

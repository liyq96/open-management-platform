package com.openplatform.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.openplatform.common.core.constant.CommonConstants;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

class RequestIdGlobalFilterTest {

    private final RequestIdGlobalFilter filter = new RequestIdGlobalFilter();

    @Test
    void shouldReuseRequestIdAndForwardIt() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/system/users")
                        .header(CommonConstants.REQUEST_ID_HEADER, "request-1"));
        WebFilterChain chain = filteredExchange -> {
            assertEquals("request-1", filteredExchange.getRequest().getHeaders()
                    .getFirst(CommonConstants.REQUEST_ID_HEADER));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertEquals("request-1", exchange.getResponse().getHeaders()
                .getFirst(CommonConstants.REQUEST_ID_HEADER));
        assertNotNull(exchange.getAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE));
    }
}

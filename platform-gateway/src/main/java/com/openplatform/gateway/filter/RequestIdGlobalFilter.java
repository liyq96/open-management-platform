package com.openplatform.gateway.filter;

import com.openplatform.common.core.constant.CommonConstants;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 为网关请求生成或复用请求编号。
 */
@Component
public class RequestIdGlobalFilter implements WebFilter, Ordered {

    private static final String VALID_REQUEST_ID_PATTERN = "[A-Za-z0-9._-]{1,128}";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = resolveRequestId(exchange);
        ServerWebExchange requestExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(
                        CommonConstants.REQUEST_ID_HEADER, requestId)))
                .build();
        requestExchange.getResponse().getHeaders().set(CommonConstants.REQUEST_ID_HEADER, requestId);
        requestExchange.getAttributes().put(CommonConstants.REQUEST_ID_ATTRIBUTE, requestId);
        return chain.filter(requestExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(CommonConstants.REQUEST_ID_HEADER);
        if (StringUtils.hasText(requestId) && requestId.matches(VALID_REQUEST_ID_PATTERN)) {
            return requestId;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}

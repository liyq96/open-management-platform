package com.openplatform.gateway.filter;

import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.core.security.InternalAccessSigner;
import com.openplatform.gateway.config.GatewayInternalAccessProperties;
import java.time.Clock;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 清除外部伪造的内部访问头，并为 Gateway 转发请求生成短时签名。
 */
public class GatewayInternalAccessFilter implements WebFilter, Ordered {

    private final GatewayInternalAccessProperties properties;
    private final Clock clock;

    public GatewayInternalAccessFilter(GatewayInternalAccessProperties properties, Clock clock) {
        if (properties.isEnabled()) {
            InternalAccessSigner.validateSecret(properties.getSecret());
        }
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchange filteredExchange = exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(CommonConstants.GATEWAY_TIMESTAMP_HEADER);
                    headers.remove(CommonConstants.GATEWAY_SIGNATURE_HEADER);
                    if (properties.isEnabled()) {
                        long timestamp = clock.millis();
                        String requestId = headers.getFirst(CommonConstants.REQUEST_ID_HEADER);
                        headers.set(CommonConstants.GATEWAY_TIMESTAMP_HEADER, Long.toString(timestamp));
                        headers.set(CommonConstants.GATEWAY_SIGNATURE_HEADER,
                                InternalAccessSigner.sign(properties.getSecret(), timestamp, requestId));
                    }
                }))
                .build();
        return chain.filter(filteredExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}

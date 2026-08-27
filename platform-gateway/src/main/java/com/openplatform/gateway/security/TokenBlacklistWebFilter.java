package com.openplatform.gateway.security;

import java.util.Optional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 拒绝已经退出或被加入黑名单的 Token。
 */
@RequiredArgsConstructor
public class TokenBlacklistWebFilter implements WebFilter {

    private final ReactiveStringRedisTemplate redisTemplate;

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;

    private final String blacklistPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .map(principal -> principal instanceof JwtAuthenticationToken jwtAuthentication
                        ? Optional.ofNullable(jwtAuthentication.getToken().getId())
                        : Optional.<String>empty())
                .defaultIfEmpty(Optional.empty())
                .flatMap(tokenId -> tokenId
                        .map(id -> redisTemplate.hasKey(blacklistPrefix + id)
                                .flatMap(blacklisted -> Boolean.TRUE.equals(blacklisted)
                                        ? authenticationEntryPoint.commence(
                                                exchange, new BadCredentialsException("Token has been revoked"))
                                        : chain.filter(exchange)))
                        .orElseGet(() -> chain.filter(exchange)));
    }
}

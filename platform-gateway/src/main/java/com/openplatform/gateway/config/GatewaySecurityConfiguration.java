package com.openplatform.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.security.properties.JwtProperties;
import com.openplatform.common.security.support.RsaPemKeyLoader;
import com.openplatform.common.security.validation.JwtAudienceValidator;
import com.openplatform.gateway.security.JsonAccessDeniedHandler;
import com.openplatform.gateway.security.JsonAuthenticationEntryPoint;
import com.openplatform.gateway.security.TokenBlacklistWebFilter;
import com.openplatform.gateway.filter.GatewayInternalAccessFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway JWT 和 Token 黑名单配置。
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties({
        GatewaySecurityProperties.class,
        GatewayInternalAccessProperties.class,
        JwtProperties.class})
public class GatewaySecurityConfiguration {

    private static final String TOKEN_BLACKLIST_PREFIX = "security:token:blacklist:";

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            GatewaySecurityProperties properties,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler,
            TokenBlacklistWebFilter tokenBlacklistWebFilter) {
        String[] permitPaths = properties.getPermitPaths().toArray(String[]::new);
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(permitPaths).permitAll()
                        .anyExchange().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> {
                        })
                        .authenticationEntryPoint(authenticationEntryPoint))
                .addFilterAfter(tokenBlacklistWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public RSAPublicKey gatewayRsaPublicKey(GatewaySecurityProperties properties) throws IOException {
        if (properties.getPublicKey() == null || !properties.getPublicKey().exists()) {
            throw new IllegalStateException("Gateway RSA public key resource does not exist");
        }
        String pem = properties.getPublicKey().getContentAsString(StandardCharsets.UTF_8);
        return RsaPemKeyLoader.loadPublicKey(pem);
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(RSAPublicKey publicKey, JwtProperties jwtProperties) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer()),
                new JwtAudienceValidator(jwtProperties.getAudience()));
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    public JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JsonAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public JsonAccessDeniedHandler jsonAccessDeniedHandler(ObjectMapper objectMapper) {
        return new JsonAccessDeniedHandler(objectMapper);
    }

    @Bean
    public TokenBlacklistWebFilter tokenBlacklistWebFilter(
            ReactiveStringRedisTemplate redisTemplate,
            JsonAuthenticationEntryPoint authenticationEntryPoint) {
        return new TokenBlacklistWebFilter(
                redisTemplate,
                authenticationEntryPoint,
                TOKEN_BLACKLIST_PREFIX);
    }

    @Bean
    public GatewayInternalAccessFilter gatewayInternalAccessFilter(
            GatewayInternalAccessProperties properties) {
        return new GatewayInternalAccessFilter(properties, Clock.systemUTC());
    }
}

package com.openplatform.system.config;

import com.openplatform.common.security.properties.JwtProperties;
import com.openplatform.common.database.tenant.TenantContextHolder;
import com.openplatform.common.security.support.RsaPemKeyLoader;
import com.openplatform.common.security.validation.JwtAudienceValidator;
import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.system.security.service.UserPermissionService;
import com.openplatform.system.security.validation.SystemUserTokenValidator;
import com.openplatform.system.tenant.security.PlatformResourcePolicy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 系统服务 JWT 资源服务器配置。
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({SystemSecurityProperties.class, JwtProperties.class})
public class SystemSecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain systemSecurityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter systemJwtAuthenticationConverter(UserPermissionService service) {
        JwtAuthenticationConverter converter=new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Number user=jwt.getClaim(JwtClaimConstants.USER_ID);Number tenant=jwt.getClaim(JwtClaimConstants.TENANT_ID);Number version=jwt.getClaim(JwtClaimConstants.AUTH_VERSION);
            boolean platformAdmin = Boolean.TRUE.equals(jwt.getClaim(JwtClaimConstants.PLATFORM_ADMIN));
            try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(tenant.longValue())) {
                return service.loadPermissions(user.longValue(),tenant.longValue(),version.intValue())
                        .stream()
                        .filter(permission -> platformAdmin
                                || !PlatformResourcePolicy.isPlatformPermission(permission))
                        .<GrantedAuthority>map(SimpleGrantedAuthority::new).toList();
            }
        });
        return converter;
    }

    @Bean
    public NimbusJwtDecoder systemJwtDecoder(
            SystemSecurityProperties properties,
            JwtProperties jwtProperties,
            UserPermissionService userPermissionService) throws IOException {
        if (properties.getPublicKey() == null || !properties.getPublicKey().exists()) {
            throw new IllegalStateException("System RSA public key resource does not exist");
        }
        RSAPublicKey publicKey = RsaPemKeyLoader.loadPublicKey(
                properties.getPublicKey().getContentAsString(StandardCharsets.UTF_8));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer()),
                new JwtAudienceValidator(jwtProperties.getAudience()),
                new SystemUserTokenValidator(userPermissionService)));
        return decoder;
    }
}

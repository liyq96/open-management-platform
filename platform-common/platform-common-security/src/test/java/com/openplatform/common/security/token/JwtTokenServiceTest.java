package com.openplatform.common.security.token;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.common.security.model.LoginUser;
import com.openplatform.common.security.properties.JwtProperties;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtTokenServiceTest {

    @Test
    void shouldIssueVerifiableRs256Token() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("test-key").build();
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
        JwtProperties properties = new JwtProperties();
        Instant now = Instant.now();
        JwtTokenService service = new JwtTokenService(
                encoder, properties, Clock.fixed(now, java.time.ZoneOffset.UTC));
        LoginUser loginUser = LoginUser.builder()
                .userId(10001L)
                .username("admin")
                .tenantId(20001L)
                .departmentId(30001L)
                .authVersion(3)
                .platformAdmin(true)
                .build();

        IssuedToken issuedToken = service.issue(loginUser);
        Jwt jwt = NimbusJwtDecoder.withPublicKey(publicKey).build().decode(issuedToken.getTokenValue());

        assertEquals(10001L, ((Number) jwt.getClaim(JwtClaimConstants.USER_ID)).longValue());
        assertEquals("admin", jwt.getClaimAsString(JwtClaimConstants.USERNAME));
        assertEquals(20001L, ((Number) jwt.getClaim(JwtClaimConstants.TENANT_ID)).longValue());
        assertEquals(true, jwt.getClaim(JwtClaimConstants.PLATFORM_ADMIN));
        assertEquals(now, issuedToken.getIssuedAt());
        assertEquals(now.plusSeconds(3600), issuedToken.getExpiresAt());
    }
}

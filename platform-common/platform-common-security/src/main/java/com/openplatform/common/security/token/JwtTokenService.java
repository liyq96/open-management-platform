package com.openplatform.common.security.token;

import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.common.security.model.LoginUser;
import com.openplatform.common.security.properties.JwtProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

/**
 * 使用 RS256 签发平台访问 Token。
 */
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    private final JwtProperties jwtProperties;

    private final Clock clock;

    public IssuedToken issue(LoginUser loginUser) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenValidity());
        String tokenId = UUID.randomUUID().toString();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .id(tokenId)
                .issuer(jwtProperties.getIssuer())
                .audience(java.util.List.of(jwtProperties.getAudience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(String.valueOf(loginUser.getUserId()))
                .claim(JwtClaimConstants.USER_ID, loginUser.getUserId())
                .claim(JwtClaimConstants.USERNAME, loginUser.getUsername())
                .claim(JwtClaimConstants.TENANT_ID, loginUser.getTenantId())
                .claim(JwtClaimConstants.PLATFORM_ADMIN, Boolean.TRUE.equals(loginUser.getPlatformAdmin()))
                .claim(JwtClaimConstants.AUTH_VERSION, loginUser.getAuthVersion());
        if (loginUser.getDepartmentId() != null) {
            claimsBuilder.claim(JwtClaimConstants.DEPARTMENT_ID, loginUser.getDepartmentId());
        }
        JwtClaimsSet claims = claimsBuilder.build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(tokenValue, tokenId, issuedAt, expiresAt);
    }
}

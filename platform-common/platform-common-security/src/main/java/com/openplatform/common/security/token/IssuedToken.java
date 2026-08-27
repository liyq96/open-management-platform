package com.openplatform.common.security.token;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 已签发的访问 Token。
 */
@Getter
@AllArgsConstructor
public class IssuedToken {

    private String tokenValue;

    private String tokenId;

    private Instant issuedAt;

    private Instant expiresAt;
}

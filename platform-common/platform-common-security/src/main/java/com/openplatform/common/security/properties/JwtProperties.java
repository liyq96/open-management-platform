package com.openplatform.common.security.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 平台 JWT 配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.security.jwt")
public class JwtProperties {

    private String issuer = "open-management-platform";

    private String audience = "platform-api";

    private Duration accessTokenValidity = Duration.ofMinutes(60);
}

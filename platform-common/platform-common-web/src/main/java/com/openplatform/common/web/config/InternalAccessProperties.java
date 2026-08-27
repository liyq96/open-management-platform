package com.openplatform.common.web.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内部服务仅允许 Gateway 访问的配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.internal-access")
public class InternalAccessProperties {

    private boolean enabled;

    private String secret;

    private Duration maxClockSkew = Duration.ofSeconds(30);
}

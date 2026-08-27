package com.openplatform.system.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * 系统服务安全配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.system.security")
public class SystemSecurityProperties {

    private Resource publicKey;

    private Duration localAccessCacheTtl = Duration.ofSeconds(30);

    private long localAccessCacheMaximumSize = 10_000;
}

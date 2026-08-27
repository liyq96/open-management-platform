package com.openplatform.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gateway 转发到内部服务时的签名配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.internal-access")
public class GatewayInternalAccessProperties {

    private boolean enabled;

    private String secret;
}

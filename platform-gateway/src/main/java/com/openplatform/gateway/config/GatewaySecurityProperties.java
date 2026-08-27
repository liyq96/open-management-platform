package com.openplatform.gateway.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * 网关安全配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.gateway.security")
public class GatewaySecurityProperties {

    private Resource publicKey;

    private List<String> permitPaths = new ArrayList<>(List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/captcha",
            "/actuator/health/**"));
}

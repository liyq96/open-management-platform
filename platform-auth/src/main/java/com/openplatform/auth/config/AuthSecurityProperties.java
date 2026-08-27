package com.openplatform.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * 认证服务 RSA 密钥配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "platform.auth.rsa")
public class AuthSecurityProperties {

    private Resource privateKey;

    private Resource publicKey;
}

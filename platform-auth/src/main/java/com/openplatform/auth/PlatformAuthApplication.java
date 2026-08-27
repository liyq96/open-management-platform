package com.openplatform.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 平台认证服务启动类。
 */
@MapperScan("com.openplatform.auth.**.mapper")
@ConfigurationPropertiesScan
@SpringBootApplication
public class PlatformAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformAuthApplication.class, args);
    }
}

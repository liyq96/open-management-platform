package com.openplatform.common.web.autoconfigure;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.openplatform.common.web.exception.GlobalExceptionHandler;
import com.openplatform.common.web.config.InternalAccessProperties;
import com.openplatform.common.web.filter.InternalAccessFilter;
import com.openplatform.common.web.filter.RequestIdFilter;
import com.openplatform.common.web.response.ApiResponseBodyAdvice;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 平台 Web 公共能力自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(InternalAccessProperties.class)
public class PlatformWebAutoConfiguration {

    /**
     * JavaScript cannot precisely represent snowflake IDs as JSON numbers.
     * Serialize all boxed Long values as strings at the HTTP boundary.
     */
    @Bean
    public Module platformLongIdJacksonModule() {
        SimpleModule module = new SimpleModule("platform-long-id-module");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        return module;
    }

    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "platform.internal-access", name = "enabled", havingValue = "true")
    public InternalAccessFilter internalAccessFilter(
            InternalAccessProperties properties,
            ObjectMapper objectMapper) {
        return new InternalAccessFilter(properties, objectMapper, Clock.systemUTC());
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ApiResponseBodyAdvice apiResponseBodyAdvice() {
        return new ApiResponseBodyAdvice();
    }
}

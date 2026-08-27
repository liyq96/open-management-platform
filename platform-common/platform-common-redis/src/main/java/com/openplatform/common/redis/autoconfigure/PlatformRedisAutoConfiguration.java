package com.openplatform.common.redis.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.redis.service.RedisCacheService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 5 公共缓存能力自动配置。
 */
@AutoConfiguration
public class PlatformRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisCacheService redisCacheService(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper) {
        return new RedisCacheService(stringRedisTemplate, objectMapper);
    }
}

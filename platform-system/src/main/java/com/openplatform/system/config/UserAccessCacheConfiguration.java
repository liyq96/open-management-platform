package com.openplatform.system.config;

import com.openplatform.common.redis.constant.RedisKeyConstants;
import com.openplatform.system.security.service.UserAccessCacheInvalidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * System 多实例访问缓存失效订阅配置。
 */
@Configuration
public class UserAccessCacheConfiguration {

    @Bean
    public RedisMessageListenerContainer userAccessCacheListenerContainer(
            RedisConnectionFactory connectionFactory,
            UserAccessCacheInvalidationService invalidationService) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(invalidationService,
                new ChannelTopic(RedisKeyConstants.USER_ACCESS_INVALIDATION_CHANNEL));
        return container;
    }
}

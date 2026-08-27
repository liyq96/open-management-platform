package com.openplatform.system.security.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.openplatform.common.redis.constant.RedisKeyConstants;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

class UserAccessCacheInvalidationServiceTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final UserPermissionService permissionService = mock(UserPermissionService.class);
    private final UserAccessCacheInvalidationService service =
            new UserAccessCacheInvalidationService(redisTemplate, permissionService);

    @Test
    void shouldEvictLocallyAndPublishUserInvalidation() {
        service.invalidateUserAfterCommit(10L, 1L);

        verify(permissionService).evictUser(10L, 1L);
        verify(redisTemplate).convertAndSend(
                RedisKeyConstants.USER_ACCESS_INVALIDATION_CHANNEL, "10:1");
    }

    @Test
    void shouldConsumeTenantInvalidationMessage() {
        Message message = mock(Message.class);
        org.mockito.Mockito.when(message.getBody())
                .thenReturn("10:*".getBytes(StandardCharsets.UTF_8));

        service.onMessage(message, null);

        verify(permissionService).evictTenant(10L);
    }
}

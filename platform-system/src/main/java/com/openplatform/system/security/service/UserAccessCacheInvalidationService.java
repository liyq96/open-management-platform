package com.openplatform.system.security.service;

import com.openplatform.common.redis.constant.RedisKeyConstants;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 在事务提交后发布访问缓存失效消息，并消费所有 System 实例的消息。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccessCacheInvalidationService implements MessageListener {

    private static final String TENANT_WILDCARD = "*";

    private final StringRedisTemplate redisTemplate;
    private final UserPermissionService userPermissionService;

    public void invalidateUserAfterCommit(Long tenantId, Long userId) {
        afterCommit(() -> {
            userPermissionService.evictUser(tenantId, userId);
            publish(tenantId + ":" + userId);
        });
    }

    public void invalidateTenantAfterCommit(Long tenantId) {
        afterCommit(() -> {
            userPermissionService.evictTenant(tenantId);
            publish(tenantId + ":" + TENANT_WILDCARD);
        });
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        String[] parts = payload.split(":", 2);
        if (parts.length != 2) {
            return;
        }
        try {
            Long tenantId = Long.valueOf(parts[0]);
            if (TENANT_WILDCARD.equals(parts[1])) {
                userPermissionService.evictTenant(tenantId);
            } else {
                userPermissionService.evictUser(tenantId, Long.valueOf(parts[1]));
            }
        } catch (NumberFormatException ignored) {
            // Ignore malformed messages from the shared Redis channel.
        }
    }

    private void publish(String payload) {
        try {
            redisTemplate.convertAndSend(RedisKeyConstants.USER_ACCESS_INVALIDATION_CHANNEL, payload);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish user access cache invalidation; remote nodes will use TTL fallback");
        }
    }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}

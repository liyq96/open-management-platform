package com.openplatform.system.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.openplatform.common.redis.constant.RedisKeyConstants;
import com.openplatform.common.redis.service.RedisCacheService;
import com.openplatform.common.security.model.UserPermissionCache;
import com.openplatform.system.security.mapper.UserAccessMapper;
import com.openplatform.system.config.SystemSecurityProperties;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserPermissionServiceTest {

    private final UserAccessMapper mapper = mock(UserAccessMapper.class);
    private final RedisCacheService redis = mock(RedisCacheService.class);
    private final UserPermissionService service = new UserPermissionService(
            mapper, redis, new SystemSecurityProperties());

    @Test
    void shouldUseCacheWhenTenantMatches() {
        String key = RedisKeyConstants.userPermissionKey(1L, 2);
        Set<String> permissions = new LinkedHashSet<>(Set.of("system:user:list"));
        when(redis.get(key, UserPermissionCache.class)).thenReturn(
                Optional.of(new UserPermissionCache(1L, 10L, 2, permissions)));

        assertEquals(permissions, service.loadPermissions(1L, 10L, 2));
        assertEquals(permissions, service.loadPermissions(1L, 10L, 2));
        verify(redis, times(1)).get(key, UserPermissionCache.class);
    }

    @Test
    void shouldLoadDatabaseAndWriteCacheWhenMissing() {
        String key = RedisKeyConstants.userPermissionKey(1L, 2);
        when(redis.get(key, UserPermissionCache.class)).thenReturn(Optional.empty());
        when(mapper.selectPermissionCodes(1L)).thenReturn(List.of("system:user:list"));

        assertEquals(Set.of("system:user:list"), service.loadPermissions(1L, 10L, 2));
        verify(mapper).selectPermissionCodes(1L);
    }

    @Test
    void shouldUseLocalCacheAndReloadAfterEviction() {
        when(mapper.selectAuthVersion(1L)).thenReturn(3);

        assertEquals(true, service.isAuthVersionCurrent(1L, 10L, 3));
        assertEquals(true, service.isAuthVersionCurrent(1L, 10L, 3));
        verify(mapper, times(1)).selectAuthVersion(1L);

        service.evictUser(10L, 1L);
        assertEquals(true, service.isAuthVersionCurrent(1L, 10L, 3));
        verify(mapper, times(2)).selectAuthVersion(1L);
    }
}

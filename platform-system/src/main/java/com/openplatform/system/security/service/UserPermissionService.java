package com.openplatform.system.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openplatform.common.redis.constant.RedisKeyConstants;
import com.openplatform.common.redis.service.RedisCacheService;
import com.openplatform.common.security.model.UserPermissionCache;
import com.openplatform.system.config.SystemSecurityProperties;
import com.openplatform.system.security.mapper.UserAccessMapper;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Value;
import org.springframework.stereotype.Service;

/**
 * 用户认证版本和权限的两级缓存服务。
 */
@Service
public class UserPermissionService {

    private static final Duration REDIS_CACHE_TTL = Duration.ofMinutes(30);

    private final UserAccessMapper userAccessMapper;
    private final RedisCacheService redisCacheService;
    private final Cache<UserCacheKey, Integer> authVersionCache;
    private final Cache<UserVersionCacheKey, Set<String>> permissionCache;

    public UserPermissionService(
            UserAccessMapper userAccessMapper,
            RedisCacheService redisCacheService,
            SystemSecurityProperties properties) {
        validateCacheProperties(properties);
        this.userAccessMapper = userAccessMapper;
        this.redisCacheService = redisCacheService;
        this.authVersionCache = Caffeine.newBuilder()
                .maximumSize(properties.getLocalAccessCacheMaximumSize())
                .expireAfterWrite(properties.getLocalAccessCacheTtl())
                .build();
        this.permissionCache = Caffeine.newBuilder()
                .maximumSize(properties.getLocalAccessCacheMaximumSize())
                .expireAfterWrite(properties.getLocalAccessCacheTtl())
                .build();
    }

    public boolean isAuthVersionCurrent(Long userId, Long tenantId, Integer tokenVersion) {
        Integer currentVersion = authVersionCache.get(
                new UserCacheKey(tenantId, userId),
                ignored -> userAccessMapper.selectAuthVersion(userId));
        return currentVersion != null && currentVersion.intValue() == tokenVersion.intValue();
    }

    public Set<String> loadPermissions(Long userId, Long tenantId, Integer authVersion) {
        UserVersionCacheKey localKey = new UserVersionCacheKey(tenantId, userId, authVersion);
        return permissionCache.get(localKey,
                ignored -> loadPermissionsFromSharedCache(userId, tenantId, authVersion));
    }

    public void evictUser(Long tenantId, Long userId) {
        authVersionCache.invalidate(new UserCacheKey(tenantId, userId));
        permissionCache.asMap().keySet().removeIf(
                key -> key.getTenantId().equals(tenantId) && key.getUserId().equals(userId));
    }

    public void evictTenant(Long tenantId) {
        authVersionCache.asMap().keySet().removeIf(key -> key.getTenantId().equals(tenantId));
        permissionCache.asMap().keySet().removeIf(key -> key.getTenantId().equals(tenantId));
    }

    private Set<String> loadPermissionsFromSharedCache(Long userId, Long tenantId, Integer authVersion) {
        String key = RedisKeyConstants.userPermissionKey(userId, authVersion);
        var cached = redisCacheService.get(key, UserPermissionCache.class);
        if (cached.isPresent() && tenantId.equals(cached.get().getTenantId())) {
            return Set.copyOf(cached.get().getPermissions());
        }
        Set<String> permissions = Set.copyOf(new LinkedHashSet<>(userAccessMapper.selectPermissionCodes(userId)));
        redisCacheService.set(key,
                new UserPermissionCache(userId, tenantId, authVersion, permissions),
                REDIS_CACHE_TTL);
        return permissions;
    }

    private void validateCacheProperties(SystemSecurityProperties properties) {
        if (properties.getLocalAccessCacheTtl() == null
                || properties.getLocalAccessCacheTtl().isZero()
                || properties.getLocalAccessCacheTtl().isNegative()) {
            throw new IllegalStateException("platform.system.security.local-access-cache-ttl must be positive");
        }
        if (properties.getLocalAccessCacheMaximumSize() <= 0) {
            throw new IllegalStateException("platform.system.security.local-access-cache-maximum-size must be positive");
        }
    }

    @Value
    private static class UserCacheKey {
        Long tenantId;
        Long userId;
    }

    @Value
    private static class UserVersionCacheKey {
        Long tenantId;
        Long userId;
        Integer authVersion;
    }
}

package com.openplatform.common.redis.constant;

/**
 * 平台 Redis Key 前缀。
 */
public final class RedisKeyConstants {

    public static final String TOKEN_BLACKLIST_PREFIX = "security:token:blacklist:";

    public static final String AUTH_VERSION_PREFIX = "security:user:auth-version:";

    public static final String USER_PERMISSION_PREFIX = "security:user:permission:";

    public static final String USER_ACCESS_INVALIDATION_CHANNEL = "security:user:access:invalidate";

    public static String userPermissionKey(Long userId, Integer authVersion) {
        return USER_PERMISSION_PREFIX + userId + ":" + authVersion;
    }

    public static final String LOGIN_FAILURE_PREFIX = "security:login:failure:";

    public static final String CAPTCHA_PREFIX = "security:captcha:";

    public static String captchaKey(String captchaId) {
        return CAPTCHA_PREFIX + captchaId;
    }

    private RedisKeyConstants() {
    }
}

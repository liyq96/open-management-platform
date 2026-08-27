package com.openplatform.common.redis.exception;

/**
 * Redis 缓存序列化或读取异常。
 */
public class RedisCacheException extends RuntimeException {

    public RedisCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}

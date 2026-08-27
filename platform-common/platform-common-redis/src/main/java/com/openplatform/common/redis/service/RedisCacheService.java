package com.openplatform.common.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openplatform.common.redis.exception.RedisCacheException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;

/**
 * 基于 StringRedisTemplate 的通用 JSON 缓存服务。
 *
 * <p>仅使用 Redis 5 支持的基础命令，避免使用高版本专属能力。</p>
 */
@RequiredArgsConstructor
public class RedisCacheService {

    private static final DefaultRedisScript<String> GET_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('GET', KEYS[1]); "
                    + "if value then redis.call('DEL', KEYS[1]); end; "
                    + "return value",
            String.class);

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    public void set(String key, Object value, Duration timeout) {
        validateKey(key);
        Assert.notNull(value, "Redis cache value must not be null");
        Assert.notNull(timeout, "Redis cache timeout must not be null");
        Assert.isTrue(!timeout.isNegative() && !timeout.isZero(), "Redis cache timeout must be positive");
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), timeout);
        } catch (JsonProcessingException exception) {
            throw new RedisCacheException("Failed to serialize Redis cache value", exception);
        }
    }

    public <T> Optional<T> get(String key, Class<T> valueType) {
        validateKey(key);
        Assert.notNull(valueType, "Redis cache value type must not be null");
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, valueType));
        } catch (JsonProcessingException exception) {
            throw new RedisCacheException("Failed to deserialize Redis cache value", exception);
        }
    }

    /**
     * 原子读取并删除缓存，适用于验证码等只能消费一次的数据。
     */
    public <T> Optional<T> getAndDelete(String key, Class<T> valueType) {
        validateKey(key);
        Assert.notNull(valueType, "Redis cache value type must not be null");
        String value = stringRedisTemplate.execute(
                GET_AND_DELETE_SCRIPT, Collections.singletonList(key));
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, valueType));
        } catch (JsonProcessingException exception) {
            throw new RedisCacheException("Failed to deserialize Redis cache value", exception);
        }
    }

    public boolean delete(String key) {
        validateKey(key);
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    public boolean hasKey(String key) {
        validateKey(key);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public long increment(String key, long delta) {
        validateKey(key);
        Long result = stringRedisTemplate.opsForValue().increment(key, delta);
        if (result == null) {
            throw new RedisCacheException("Redis increment returned no result", null);
        }
        return result;
    }

    public boolean expire(String key, Duration timeout) {
        validateKey(key);
        Assert.notNull(timeout, "Redis cache timeout must not be null");
        Assert.isTrue(!timeout.isNegative() && !timeout.isZero(), "Redis cache timeout must be positive");
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout));
    }

    private void validateKey(String key) {
        Assert.hasText(key, "Redis cache key must not be blank");
    }
}

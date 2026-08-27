package com.openplatform.common.redis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisCacheServiceTest {

    private final StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        redisCacheService = new RedisCacheService(stringRedisTemplate, new ObjectMapper());
    }

    @Test
    void shouldWriteJsonWithTimeout() {
        Duration timeout = Duration.ofMinutes(10);

        redisCacheService.set("user:1", new UserCacheVO(1L, "admin"), timeout);

        verify(valueOperations).set("user:1", "{\"userId\":1,\"username\":\"admin\"}", timeout);
    }

    @Test
    void shouldReadJsonValue() {
        when(valueOperations.get("user:1")).thenReturn("{\"userId\":1,\"username\":\"admin\"}");

        Optional<UserCacheVO> result = redisCacheService.get("user:1", UserCacheVO.class);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserId());
        assertEquals("admin", result.get().getUsername());
    }

    @Test
    void shouldAtomicallyReadAndDeleteJsonValue() {
        when(stringRedisTemplate.execute(any(), eq(java.util.List.of("captcha:1"))))
                .thenReturn("\"ABCD\"");

        Optional<String> result = redisCacheService.getAndDelete("captcha:1", String.class);

        assertTrue(result.isPresent());
        assertEquals("ABCD", result.get());
    }

    public static class UserCacheVO {

        private Long userId;

        private String username;

        public UserCacheVO() {
        }

        public UserCacheVO(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}

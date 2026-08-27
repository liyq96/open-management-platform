package com.openplatform.auth.captcha.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openplatform.auth.captcha.model.vo.CaptchaVO;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.redis.service.RedisCacheService;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CaptchaServiceImplTest {

    private final RedisCacheService redisCacheService = mock(RedisCacheService.class);
    private final CaptchaServiceImpl captchaService = new CaptchaServiceImpl(redisCacheService);

    @Test
    void shouldGenerateCaptchaImageAndStoreAnswer() {
        CaptchaVO result = captchaService.generate();

        assertTrue(result.getImageBase64().startsWith("data:image/png;base64,"));
        assertEquals(120, result.getExpiresIn());
        verify(redisCacheService).set(startsWith("security:captcha:"), any(String.class),
                any(Duration.class));
    }

    @Test
    void shouldIgnoreLetterCaseAndConsumeCaptcha() {
        when(redisCacheService.getAndDelete("security:captcha:captcha-id", String.class))
                .thenReturn(Optional.of("ABCD"));

        captchaService.validate("captcha-id", "abcd");

        verify(redisCacheService).getAndDelete("security:captcha:captcha-id", String.class);
    }

    @Test
    void shouldRejectMissingCaptcha() {
        when(redisCacheService.getAndDelete("security:captcha:captcha-id", String.class))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> captchaService.validate("captcha-id", "ABCD"));

        assertEquals("AUTH_004", exception.getCode());
    }
}

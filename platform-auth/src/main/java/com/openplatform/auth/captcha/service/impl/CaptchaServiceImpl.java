package com.openplatform.auth.captcha.service.impl;

import com.openplatform.auth.captcha.model.vo.CaptchaVO;
import com.openplatform.auth.captcha.service.CaptchaService;
import com.openplatform.auth.error.AuthErrorCode;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.redis.constant.RedisKeyConstants;
import com.openplatform.common.redis.service.RedisCacheService;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 基于 Redis 的一次性图形验证码。 */
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final char[] CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int CODE_LENGTH = 4;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;
    private static final Duration VALIDITY = Duration.ofMinutes(2);

    private final RedisCacheService redisCacheService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public CaptchaVO generate() {
        String captchaId = UUID.randomUUID().toString();
        String code = randomCode();
        redisCacheService.set(RedisKeyConstants.captchaKey(captchaId), code, VALIDITY);
        return new CaptchaVO(captchaId, render(code), VALIDITY.toSeconds());
    }

    @Override
    public void validate(String captchaId, String captchaCode) {
        String expected = redisCacheService
                .getAndDelete(RedisKeyConstants.captchaKey(captchaId), String.class)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.CAPTCHA_INVALID));
        if (!expected.equals(captchaCode.trim().toUpperCase(Locale.ROOT))) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_INVALID);
        }
    }

    private String randomCode() {
        StringBuilder result = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            result.append(CHARACTERS[secureRandom.nextInt(CHARACTERS.length)]);
        }
        return result.toString();
    }

    private String render(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            drawNoise(graphics);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            for (int index = 0; index < code.length(); index++) {
                graphics.setColor(randomDarkColor());
                graphics.drawString(String.valueOf(code.charAt(index)), 13 + index * 26, 32);
            }
        } finally {
            graphics.dispose();
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to generate captcha image", exception);
        }
    }

    private void drawNoise(Graphics2D graphics) {
        for (int index = 0; index < 5; index++) {
            graphics.setColor(new Color(
                    120 + secureRandom.nextInt(100),
                    120 + secureRandom.nextInt(100),
                    120 + secureRandom.nextInt(100)));
            graphics.drawLine(
                    secureRandom.nextInt(WIDTH), secureRandom.nextInt(HEIGHT),
                    secureRandom.nextInt(WIDTH), secureRandom.nextInt(HEIGHT));
        }
    }

    private Color randomDarkColor() {
        return new Color(
                secureRandom.nextInt(100),
                secureRandom.nextInt(100),
                secureRandom.nextInt(100));
    }
}

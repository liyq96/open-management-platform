package com.openplatform.auth.captcha.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 图形验证码响应。 */
@Getter
@AllArgsConstructor
public class CaptchaVO {

    private String captchaId;

    private String imageBase64;

    private long expiresIn;
}

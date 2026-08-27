package com.openplatform.auth.captcha.service;

import com.openplatform.auth.captcha.model.vo.CaptchaVO;

/** 图形验证码服务。 */
public interface CaptchaService {

    CaptchaVO generate();

    void validate(String captchaId, String captchaCode);
}

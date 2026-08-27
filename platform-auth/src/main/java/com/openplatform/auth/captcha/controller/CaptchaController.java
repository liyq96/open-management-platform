package com.openplatform.auth.captcha.controller;

import com.openplatform.auth.captcha.model.vo.CaptchaVO;
import com.openplatform.auth.captcha.service.CaptchaService;
import com.openplatform.common.core.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 图形验证码接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping
    public ApiResponse<CaptchaVO> generate() {
        return ApiResponse.success(captchaService.generate());
    }
}

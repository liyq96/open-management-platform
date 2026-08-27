package com.openplatform.auth.login.controller;

import com.openplatform.auth.login.model.dto.UserLoginDTO;
import com.openplatform.auth.login.model.LoginClientInfo;
import com.openplatform.auth.login.model.vo.UserLoginVO;
import com.openplatform.auth.login.service.AuthService;
import com.openplatform.common.core.model.ApiResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录和退出接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<UserLoginVO> login(
            @Valid @RequestBody UserLoginDTO dto,
            HttpServletRequest request) {
        LoginClientInfo clientInfo = new LoginClientInfo(
                limit(request.getRemoteAddr(), 64), limit(request.getHeader("User-Agent"), 512));
        return ApiResponse.success(authService.login(dto, clientInfo));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(JwtAuthenticationToken authentication) {
        authService.logout(authentication.getToken());
        return ApiResponse.success(null);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

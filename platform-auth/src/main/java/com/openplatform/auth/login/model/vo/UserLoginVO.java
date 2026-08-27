package com.openplatform.auth.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户登录结果。
 */
@Getter
@AllArgsConstructor
public class UserLoginVO {

    private String token;

    private String tokenType;

    private long expiresIn;
}

package com.openplatform.auth.login.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 登录请求的非敏感客户端信息。 */
@Getter
@AllArgsConstructor
public class LoginClientInfo {
    private String loginIp;
    private String userAgent;
}

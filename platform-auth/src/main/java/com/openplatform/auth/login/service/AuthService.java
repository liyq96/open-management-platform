package com.openplatform.auth.login.service;

import com.openplatform.auth.login.model.dto.UserLoginDTO;
import com.openplatform.auth.login.model.LoginClientInfo;
import com.openplatform.auth.login.model.vo.UserLoginVO;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 登录认证服务。
 */
public interface AuthService {

    UserLoginVO login(UserLoginDTO dto, LoginClientInfo clientInfo);

    void logout(Jwt jwt);
}

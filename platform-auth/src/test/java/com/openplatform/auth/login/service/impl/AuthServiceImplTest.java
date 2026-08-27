package com.openplatform.auth.login.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import com.openplatform.auth.login.model.dto.UserLoginDTO;
import com.openplatform.auth.login.model.LoginClientInfo;
import com.openplatform.auth.login.mapper.AuthLoginLogMapper;
import com.openplatform.auth.login.mapper.AuthTenantMapper;
import com.openplatform.auth.captcha.service.CaptchaService;
import com.openplatform.auth.login.model.vo.UserLoginVO;
import com.openplatform.auth.login.security.PlatformUserDetails;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.redis.service.RedisCacheService;
import com.openplatform.common.security.token.IssuedToken;
import com.openplatform.common.security.token.JwtTokenService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class AuthServiceImplTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);

    private final RedisCacheService redisCacheService = mock(RedisCacheService.class);
    private final AuthLoginLogMapper loginLogMapper = mock(AuthLoginLogMapper.class);
    private final CaptchaService captchaService = mock(CaptchaService.class);
    private final AuthTenantMapper authTenantMapper = mock(AuthTenantMapper.class);
    private final PlatformIdGenerator idGenerator = mock(PlatformIdGenerator.class);

    private final AuthServiceImpl authService =
            new AuthServiceImpl(
                    authenticationManager, jwtTokenService, redisCacheService, loginLogMapper, idGenerator,
                    captchaService, authTenantMapper);

    private final LoginClientInfo clientInfo = new LoginClientInfo("127.0.0.1", "test");

    @Test
    void shouldReturnTokenAfterSuccessfulLogin() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setTenantCode("platform");
        dto.setUsername("admin");
        dto.setPassword("password");
        dto.setCaptchaId("captcha-id");
        dto.setCaptchaCode("ABCD");
        PlatformUserDetails principal = new PlatformUserDetails(
                1L, 2L, 3L, 1, "admin", "encoded", true);
        when(authenticationManager.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        Instant issuedAt = Instant.now();
        when(jwtTokenService.issue(any())).thenReturn(
                new IssuedToken("jwt-token", "token-id", issuedAt, issuedAt.plusSeconds(3600)));
        when(authTenantMapper.selectEnabledTenantId("platform")).thenReturn(2L);

        UserLoginVO result = authService.login(dto, clientInfo);

        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600, result.getExpiresIn());
    }

    @Test
    void shouldHideCredentialFailureDetails() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setTenantCode("platform");
        dto.setUsername("unknown");
        dto.setPassword("bad-password");
        dto.setCaptchaId("captcha-id");
        dto.setCaptchaCode("ABCD");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(authTenantMapper.selectEnabledTenantId("platform")).thenReturn(2L);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> authService.login(dto, clientInfo));

        assertEquals("AUTH_001", exception.getCode());
        assertEquals("账号或密码错误", exception.getMessage());
    }

    @Test
    void shouldRejectUnknownTenantBeforeLoadingUser() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setTenantCode("unknown");
        dto.setUsername("admin");
        dto.setPassword("password");
        dto.setCaptchaId("captcha-id");
        dto.setCaptchaCode("ABCD");

        BusinessException exception = assertThrows(
                BusinessException.class, () -> authService.login(dto, clientInfo));

        assertEquals("AUTH_001", exception.getCode());
        verifyNoInteractions(authenticationManager);
    }
}

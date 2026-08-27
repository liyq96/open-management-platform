package com.openplatform.auth.login.service.impl;

import com.openplatform.auth.error.AuthErrorCode;
import com.openplatform.auth.captcha.service.CaptchaService;
import com.openplatform.auth.login.model.dto.UserLoginDTO;
import com.openplatform.auth.login.model.LoginClientInfo;
import com.openplatform.auth.login.mapper.AuthLoginLogMapper;
import com.openplatform.auth.login.mapper.AuthTenantMapper;
import com.openplatform.auth.login.model.vo.UserLoginVO;
import com.openplatform.auth.login.security.PlatformUserDetails;
import com.openplatform.auth.login.service.AuthService;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.redis.constant.RedisKeyConstants;
import com.openplatform.common.database.tenant.TenantContextHolder;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.redis.service.RedisCacheService;
import com.openplatform.common.security.model.LoginUser;
import com.openplatform.common.security.model.UserPermissionCache;
import com.openplatform.common.security.token.IssuedToken;
import com.openplatform.common.security.token.JwtTokenService;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 账号密码登录和 Token 退出实现。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;

    private final JwtTokenService jwtTokenService;

    private final RedisCacheService redisCacheService;

    private final AuthLoginLogMapper loginLogMapper;

    private final PlatformIdGenerator idGenerator;

    private final CaptchaService captchaService;

    private final AuthTenantMapper authTenantMapper;

    @Override
    public UserLoginVO login(UserLoginDTO dto, LoginClientInfo clientInfo) {
        captchaService.validate(dto.getCaptchaId(), dto.getCaptchaCode());
        Long tenantId = authTenantMapper.selectEnabledTenantId(dto.getTenantCode());
        if (tenantId == null || tenantId <= 0) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(tenantId)) {
            try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(dto.getUsername(), dto.getPassword()));
            PlatformUserDetails userDetails = (PlatformUserDetails) authentication.getPrincipal();
            redisCacheService.set(
                    RedisKeyConstants.userPermissionKey(userDetails.getUserId(), userDetails.getAuthVersion()),
                    new UserPermissionCache(userDetails.getUserId(), userDetails.getTenantId(),
                            userDetails.getAuthVersion(), userDetails.getPermissions()),
                    Duration.ofMinutes(30));
            IssuedToken issuedToken = jwtTokenService.issue(LoginUser.builder()
                    .userId(userDetails.getUserId())
                    .username(userDetails.getUsername())
                    .tenantId(userDetails.getTenantId())
                    .departmentId(userDetails.getDepartmentId())
                    .authVersion(userDetails.getAuthVersion())
                    .platformAdmin(userDetails.isPlatformAdmin())
                    .build());
            long expiresIn = Duration.between(issuedToken.getIssuedAt(), issuedToken.getExpiresAt()).toSeconds();
            writeLoginLog(userDetails.getUserId(), dto.getUsername(),
                    clientInfo, true, null);
            return new UserLoginVO(issuedToken.getTokenValue(), "Bearer", expiresIn);
            } catch (DisabledException exception) {
                writeLoginLog(null, dto.getUsername(), clientInfo, false, "ACCOUNT_DISABLED");
                throw new BusinessException(AuthErrorCode.ACCOUNT_DISABLED);
            } catch (BadCredentialsException exception) {
                writeLoginLog(null, dto.getUsername(), clientInfo, false, "INVALID_CREDENTIALS");
                throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
            }
        }
    }

    @Override
    public void logout(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null || jwt.getId() == null) {
            throw new BusinessException(AuthErrorCode.TOKEN_INVALID);
        }
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (remaining.isPositive()) {
            redisCacheService.set(
                    RedisKeyConstants.TOKEN_BLACKLIST_PREFIX + jwt.getId(),
                    true,
                    remaining);
        }
    }

    private void writeLoginLog(Long userId, String username,
            LoginClientInfo clientInfo, boolean success, String failureReason) {
        try {
            loginLogMapper.insert(idGenerator.nextId(), userId, username, clientInfo.getLoginIp(),
                    clientInfo.getUserAgent(), success, failureReason);
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to write login audit log", exception);
        }
    }
}

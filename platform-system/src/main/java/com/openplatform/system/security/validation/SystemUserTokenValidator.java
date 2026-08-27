package com.openplatform.system.security.validation;

import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.common.database.tenant.TenantContextHolder;
import com.openplatform.system.security.service.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/** 校验 Token 中认证版本仍与数据库一致。 */
@RequiredArgsConstructor
public class SystemUserTokenValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error ERROR=new OAuth2Error("invalid_token","Token permission version is outdated",null);
    private final UserPermissionService userPermissionService;
    @Override public OAuth2TokenValidatorResult validate(Jwt jwt){
        Number user=jwt.getClaim(JwtClaimConstants.USER_ID);Number tenant=jwt.getClaim(JwtClaimConstants.TENANT_ID);Number version=jwt.getClaim(JwtClaimConstants.AUTH_VERSION);
        if(user==null||tenant==null||version==null)return OAuth2TokenValidatorResult.failure(ERROR);
        try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(tenant.longValue())) {
            return userPermissionService.isAuthVersionCurrent(
                    user.longValue(), tenant.longValue(), version.intValue())
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(ERROR);
        }
    }
}

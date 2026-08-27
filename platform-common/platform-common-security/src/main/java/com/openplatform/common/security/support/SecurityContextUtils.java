package com.openplatform.common.security.support;

import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.common.security.model.LoginUser;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * 当前登录用户上下文工具。
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
    }

    public static Optional<LoginUser> getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Map<String, Object> claims = jwtAuthentication.getTokenAttributes();
        return Optional.of(LoginUser.builder()
                .userId(asLong(claims.get(JwtClaimConstants.USER_ID)))
                .username(asString(claims.get(JwtClaimConstants.USERNAME)))
                .tenantId(asLong(claims.get(JwtClaimConstants.TENANT_ID)))
                .departmentId(asLong(claims.get(JwtClaimConstants.DEPARTMENT_ID)))
                .authVersion(asInteger(claims.get(JwtClaimConstants.AUTH_VERSION)))
                .platformAdmin(asBoolean(claims.get(JwtClaimConstants.PLATFORM_ADMIN)))
                .build());
    }

    public static Long requireUserId() {
        return getLoginUser()
                .map(LoginUser::getUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user is unavailable"));
    }

    public static Long requireTenantId() {
        return getLoginUser()
                .map(LoginUser::getTenantId)
                .orElseThrow(() -> new IllegalStateException("Authenticated tenant is unavailable"));
    }

    public static boolean isPlatformAdmin() {
        return getLoginUser()
                .map(LoginUser::getPlatformAdmin)
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }

    private static Long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private static Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Boolean asBoolean(Object value) {
        return value instanceof Boolean booleanValue ? booleanValue : false;
    }
}

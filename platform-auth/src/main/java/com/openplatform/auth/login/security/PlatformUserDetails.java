package com.openplatform.auth.login.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 登录账号信息。
 */
@Getter
public class PlatformUserDetails implements UserDetails {

    private final Long userId;

    private final Long tenantId;

    private final Long departmentId;

    private final Integer authVersion;

    private final String username;

    private final String password;

    private final boolean enabled;

    private final boolean platformAdmin;

    private final Set<String> permissions;

    public PlatformUserDetails(
            Long userId,
            Long tenantId,
            Long departmentId,
            Integer authVersion,
            String username,
            String password,
            boolean enabled) {
        this(userId, tenantId, departmentId, authVersion, username, password, enabled, Set.of());
    }

    public PlatformUserDetails(Long userId, Long tenantId, Long departmentId, Integer authVersion,
            String username, String password, boolean enabled, Set<String> permissions) {
        this(userId, tenantId, departmentId, authVersion, username, password,
                enabled, false, permissions);
    }

    public PlatformUserDetails(Long userId, Long tenantId, Long departmentId, Integer authVersion,
            String username, String password, boolean enabled, boolean platformAdmin,
            Set<String> permissions) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.departmentId = departmentId;
        this.authVersion = authVersion;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.platformAdmin = platformAdmin;
        this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

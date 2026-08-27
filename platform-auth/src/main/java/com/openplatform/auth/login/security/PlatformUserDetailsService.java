package com.openplatform.auth.login.security;

import com.openplatform.auth.login.mapper.AuthUserMapper;
import com.openplatform.auth.login.model.entity.AuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.LinkedHashSet;

/**
 * 从 PostgreSQL 加载登录账号。
 */
@Service
@RequiredArgsConstructor
public class PlatformUserDetailsService implements UserDetailsService {

    private final AuthUserMapper authUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUserInfo user = authUserMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User does not exist");
        }
        return new PlatformUserDetails(
                user.getId(),
                user.getTenantId(),
                user.getDepartmentId(),
                user.getAuthVersion(),
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                Boolean.TRUE.equals(user.getPlatformAdmin()),
                new LinkedHashSet<>(authUserMapper.selectPermissionCodes(user.getId())));
    }
}

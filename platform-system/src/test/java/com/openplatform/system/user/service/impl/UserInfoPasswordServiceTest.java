package com.openplatform.system.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.system.department.mapper.DepartmentInfoMapper;
import com.openplatform.system.user.mapper.UserInfoMapper;
import com.openplatform.system.security.service.UserAccessCacheInvalidationService;
import com.openplatform.system.user.mapper.UserPositionRelationMapper;
import com.openplatform.system.user.mapper.UserRoleRelationMapper;
import com.openplatform.system.user.model.dto.UserPasswordChangeDTO;
import com.openplatform.system.user.model.dto.UserPasswordResetDTO;
import com.openplatform.system.user.model.dto.UserUpdateDTO;
import com.openplatform.system.user.model.entity.UserInfo;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserInfoPasswordServiceTest {

    private final UserInfoMapper userInfoMapper = mock(UserInfoMapper.class);
    private final DepartmentInfoMapper departmentInfoMapper = mock(DepartmentInfoMapper.class);
    private final UserRoleRelationMapper roleRelationMapper = mock(UserRoleRelationMapper.class);
    private final UserPositionRelationMapper positionRelationMapper = mock(UserPositionRelationMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final PlatformIdGenerator idGenerator = mock(PlatformIdGenerator.class);
    private final UserAccessCacheInvalidationService cacheInvalidationService =
            mock(UserAccessCacheInvalidationService.class);

    private final UserInfoServiceImpl userInfoService = new UserInfoServiceImpl(
            userInfoMapper,
            departmentInfoMapper,
            roleRelationMapper,
            positionRelationMapper,
            passwordEncoder,
            idGenerator,
            cacheInvalidationService);

    @BeforeEach
    void authenticate() {
        Instant now = Instant.now();
        Jwt jwt = new Jwt("token", now, now.plusSeconds(60), Map.of("alg", "RS256"), Map.of(
                JwtClaimConstants.USER_ID, 1L,
                JwtClaimConstants.USERNAME, "admin",
                JwtClaimConstants.TENANT_ID, 10L,
                JwtClaimConstants.DEPARTMENT_ID, 20L,
                JwtClaimConstants.AUTH_VERSION, 1));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldChangePasswordAndIncrementAuthVersion() {
        UserInfo user = user(1L);
        when(userInfoMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        UserPasswordChangeDTO dto = changeDTO("old-password", "new-password", "new-password");

        userInfoService.changePassword(dto);

        assertEquals("new-hash", user.getPassword());
        assertEquals(4, user.getAuthVersion());
        assertEquals(1L, user.getUpdatedBy());
        verify(userInfoMapper).updateById(user);
    }

    @Test
    void shouldRejectIncorrectOldPassword() {
        when(userInfoMapper.selectOne(any())).thenReturn(user(1L));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userInfoService.changePassword(
                        changeDTO("wrong-password", "new-password", "new-password")));

        assertEquals("SYSTEM_027", exception.getCode());
    }

    @Test
    void shouldResetUserPasswordAndIncrementAuthVersion() {
        UserInfo target = user(2L);
        when(userInfoMapper.selectOne(any())).thenReturn(target);
        when(passwordEncoder.matches("reset-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("reset-password")).thenReturn("reset-hash");
        UserPasswordResetDTO dto = new UserPasswordResetDTO();
        dto.setUserId(2L);
        dto.setNewPassword("reset-password");
        dto.setConfirmPassword("reset-password");

        userInfoService.resetPassword(dto);

        assertEquals("reset-hash", target.getPassword());
        assertEquals(4, target.getAuthVersion());
        verify(userInfoMapper).updateById(target);
    }

    @Test
    void shouldNotIncrementAuthVersionWhenDepartmentChanges() {
        UserInfo target = user(2L);
        target.setDepartmentId(20L);
        target.setEnabled(true);
        when(userInfoMapper.selectOne(any())).thenReturn(target);
        when(departmentInfoMapper.selectCount(any())).thenReturn(1L);
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUserId(2L);
        dto.setDepartmentId(30L);
        dto.setDisplayName("新名称");
        dto.setEnabled(true);

        userInfoService.update(dto);

        assertEquals(30L, target.getDepartmentId());
        assertEquals(3, target.getAuthVersion());
        verifyNoInteractions(cacheInvalidationService);
    }

    private UserInfo user(Long userId) {
        UserInfo user = new UserInfo();
        user.setId(userId);
        user.setTenantId(10L);
        user.setPassword("old-hash");
        user.setAuthVersion(3);
        user.setVersion(0);
        user.setDeleted(false);
        return user;
    }

    private UserPasswordChangeDTO changeDTO(
            String oldPassword, String newPassword, String confirmPassword) {
        UserPasswordChangeDTO dto = new UserPasswordChangeDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(confirmPassword);
        return dto;
    }
}

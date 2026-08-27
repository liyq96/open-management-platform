package com.openplatform.system.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.security.constant.JwtClaimConstants;
import com.openplatform.system.department.mapper.DepartmentInfoMapper;
import com.openplatform.system.menu.mapper.MenuInfoMapper;
import com.openplatform.system.menu.mapper.MenuReferenceMapper;
import com.openplatform.system.menu.model.dto.MenuCreateDTO;
import com.openplatform.system.menu.service.impl.MenuInfoServiceImpl;
import com.openplatform.system.permission.group.mapper.PermissionGroupInfoMapper;
import com.openplatform.system.permission.group.model.entity.PermissionGroupInfo;
import com.openplatform.system.permission.mapper.PermissionInfoMapper;
import com.openplatform.system.permission.mapper.PermissionReferenceMapper;
import com.openplatform.system.permission.model.dto.PermissionUpdateDTO;
import com.openplatform.system.permission.model.entity.PermissionInfo;
import com.openplatform.system.permission.model.enums.PermissionType;
import com.openplatform.system.permission.service.impl.PermissionInfoServiceImpl;
import com.openplatform.system.role.mapper.RoleInfoMapper;
import com.openplatform.system.role.mapper.RoleRelationMapper;
import com.openplatform.system.role.model.dto.RoleMenuAssignDTO;
import com.openplatform.system.role.model.dto.RolePermissionAssignDTO;
import com.openplatform.system.role.model.entity.RoleInfo;
import com.openplatform.system.role.service.impl.RoleInfoServiceImpl;
import com.openplatform.system.user.mapper.UserInfoMapper;
import com.openplatform.system.user.mapper.UserPositionRelationMapper;
import com.openplatform.system.user.mapper.UserRoleRelationMapper;
import com.openplatform.system.user.model.dto.UserRoleAssignDTO;
import com.openplatform.system.user.model.entity.UserInfo;
import com.openplatform.system.user.service.impl.UserInfoServiceImpl;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityBoundaryMutationTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotRefreshTokenWhenOnlyPermissionMetadataChanges() {
        PermissionInfoMapper permissionMapper = mock(PermissionInfoMapper.class);
        PermissionReferenceMapper referenceMapper = mock(PermissionReferenceMapper.class);
        PermissionGroupInfoMapper groupMapper = mock(PermissionGroupInfoMapper.class);
        UserAccessCacheInvalidationService invalidationService =
                mock(UserAccessCacheInvalidationService.class);
        PermissionInfoServiceImpl service = new PermissionInfoServiceImpl(
                permissionMapper, referenceMapper, groupMapper, invalidationService);

        PermissionInfo permission = new PermissionInfo();
        permission.setId(10L);
        permission.setGroupId(1L);
        permission.setPermissionCode("system:user:list");
        permission.setPermissionName("用户查询");
        permission.setPermissionType("API");
        permission.setEnabled(true);
        when(permissionMapper.selectById(10L)).thenReturn(permission);
        when(groupMapper.selectById(2L)).thenReturn(new PermissionGroupInfo());
        when(permissionMapper.selectCount(any())).thenReturn(0L);
        authenticate(99L, 1L);

        PermissionUpdateDTO dto = new PermissionUpdateDTO();
        dto.setPermissionId(10L);
        dto.setGroupId(2L);
        dto.setPermissionCode("system:user:list");
        dto.setPermissionName("用户列表查询");
        dto.setPermissionType(PermissionType.BUTTON);
        dto.setEnabled(true);
        service.update(dto);

        verify(referenceMapper, never()).incrementReferencedUserAuthVersion(any());
        verify(invalidationService, never()).invalidateTenantAfterCommit(any());
    }

    @Test
    void shouldIgnoreUnchangedRoleRelations() {
        RoleInfoMapper roleMapper = mock(RoleInfoMapper.class);
        RoleRelationMapper relationMapper = mock(RoleRelationMapper.class);
        UserAccessCacheInvalidationService invalidationService =
                mock(UserAccessCacheInvalidationService.class);
        RoleInfoServiceImpl service = new RoleInfoServiceImpl(
                roleMapper, relationMapper, mock(PlatformIdGenerator.class), invalidationService);
        when(roleMapper.selectOne(any())).thenReturn(new RoleInfo());
        when(relationMapper.countEnabledPermissions(any())).thenReturn(2L);
        when(relationMapper.selectPermissionIds(1L)).thenReturn(List.of(2L, 3L));
        when(relationMapper.countEnabledMenus(any())).thenReturn(2L);
        when(relationMapper.selectMenuIds(1L)).thenReturn(List.of(4L, 5L));

        RolePermissionAssignDTO permissions = new RolePermissionAssignDTO();
        permissions.setRoleId(1L);
        permissions.setPermissionIds(new LinkedHashSet<>(List.of(3L, 2L)));
        service.assignPermissions(permissions);

        RoleMenuAssignDTO menus = new RoleMenuAssignDTO();
        menus.setRoleId(1L);
        menus.setMenuIds(new LinkedHashSet<>(List.of(5L, 4L)));
        service.assignMenus(menus);

        verify(relationMapper, never()).deletePermissions(any());
        verify(relationMapper, never()).deleteMenus(any());
        verify(relationMapper, never()).incrementUserAuthVersion(any());
        verify(invalidationService, never()).invalidateTenantAfterCommit(any());
    }

    @Test
    void shouldIgnoreUnchangedUserRoles() {
        UserInfoMapper userMapper = mock(UserInfoMapper.class);
        UserRoleRelationMapper roleMapper = mock(UserRoleRelationMapper.class);
        UserAccessCacheInvalidationService invalidationService =
                mock(UserAccessCacheInvalidationService.class);
        UserInfoServiceImpl service = new UserInfoServiceImpl(
                userMapper, mock(DepartmentInfoMapper.class), roleMapper,
                mock(UserPositionRelationMapper.class), mock(PasswordEncoder.class),
                mock(PlatformIdGenerator.class), invalidationService);
        when(userMapper.selectOne(any())).thenReturn(new UserInfo());
        when(roleMapper.countEnabledRoles(any())).thenReturn(2L);
        when(roleMapper.selectRoleIds(8L)).thenReturn(List.of(4L, 5L));

        UserRoleAssignDTO dto = new UserRoleAssignDTO();
        dto.setUserId(8L);
        dto.setRoleIds(new LinkedHashSet<>(List.of(5L, 4L)));
        service.assignRoles(dto);

        verify(roleMapper, never()).deleteRoles(any());
        verify(roleMapper, never()).incrementAuthVersion(any(), any());
        verify(invalidationService, never()).invalidateUserAfterCommit(any(), any());
    }

    @Test
    void shouldRejectDuplicateMenuRoute() {
        MenuInfoMapper menuMapper = mock(MenuInfoMapper.class);
        when(menuMapper.selectCount(any())).thenReturn(1L);
        MenuInfoServiceImpl service = new MenuInfoServiceImpl(
                menuMapper, mock(MenuReferenceMapper.class),
                mock(com.openplatform.system.security.mapper.UserAccessMapper.class));
        MenuCreateDTO dto = new MenuCreateDTO();
        dto.setRoutePath("/system/user");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(dto));

        assertEquals("SYSTEM_019", exception.getCode());
    }

    private void authenticate(Long userId, Long tenantId) {
        Instant now = Instant.now();
        Jwt jwt = new Jwt("token", now, now.plusSeconds(300), Map.of("alg", "none"), Map.of(
                JwtClaimConstants.USER_ID, userId,
                JwtClaimConstants.TENANT_ID, tenantId));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new JwtAuthenticationToken(jwt, List.of(), userId.toString()));
        SecurityContextHolder.setContext(context);
    }
}

package com.openplatform.system.tenant.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.openplatform.common.database.model.BaseEntity;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.database.tenant.TenantContextHolder;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.system.department.mapper.DepartmentInfoMapper;
import com.openplatform.system.menu.mapper.MenuInfoMapper;
import com.openplatform.system.menu.model.entity.MenuInfo;
import com.openplatform.system.permission.group.mapper.PermissionGroupInfoMapper;
import com.openplatform.system.permission.group.model.entity.PermissionGroupInfo;
import com.openplatform.system.permission.mapper.PermissionInfoMapper;
import com.openplatform.system.permission.model.entity.PermissionInfo;
import com.openplatform.system.position.mapper.PositionInfoMapper;
import com.openplatform.system.role.mapper.RoleInfoMapper;
import com.openplatform.system.role.mapper.RoleRelationMapper;
import com.openplatform.system.tenant.model.dto.TenantCreateDTO;
import com.openplatform.system.user.mapper.UserInfoMapper;
import com.openplatform.system.user.mapper.UserPositionRelationMapper;
import com.openplatform.system.user.mapper.UserRoleRelationMapper;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class TenantProvisioningServiceImplTest {

    private final DepartmentInfoMapper departmentMapper = mock(DepartmentInfoMapper.class);
    private final PositionInfoMapper positionMapper = mock(PositionInfoMapper.class);
    private final RoleInfoMapper roleMapper = mock(RoleInfoMapper.class);
    private final UserInfoMapper userMapper = mock(UserInfoMapper.class);
    private final PermissionGroupInfoMapper groupMapper = mock(PermissionGroupInfoMapper.class);
    private final PermissionInfoMapper permissionMapper = mock(PermissionInfoMapper.class);
    private final MenuInfoMapper menuMapper = mock(MenuInfoMapper.class);
    private final RoleRelationMapper roleRelationMapper = mock(RoleRelationMapper.class);
    private final UserRoleRelationMapper userRoleRelationMapper = mock(UserRoleRelationMapper.class);
    private final UserPositionRelationMapper userPositionRelationMapper = mock(UserPositionRelationMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AtomicLong snowflakeIds = new AtomicLong(2_055_000_000_000_000_000L);
    private final PlatformIdGenerator idGenerator =
            new PlatformIdGenerator(entity -> snowflakeIds.incrementAndGet());

    @Test
    void shouldCreateLoginReadyTenantFromCurrentTenantTemplate() {
        PermissionGroupInfo rootGroup = group(1L, null, "system");
        PermissionGroupInfo childGroup = group(2L, 1L, "system:user");
        PermissionInfo permission = permission(3L, 2L, "system:user:list");
        PermissionGroupInfo platformGroup = group(7L, 1L, "system:tenant");
        PermissionInfo platformPermission = permission(8L, 7L, "system:tenant:list");
        MenuInfo rootMenu = menu(4L, null, "/system");
        MenuInfo childMenu = menu(5L, 4L, "/system/user");
        MenuInfo unselectedMenu = menu(6L, 4L, "/system/role");
        when(groupMapper.selectList(any())).thenReturn(List.of(rootGroup, childGroup, platformGroup));
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission, platformPermission));
        when(menuMapper.selectList(any())).thenReturn(List.of(rootMenu, childMenu, unselectedMenu));
        when(passwordEncoder.encode("Admin@123456")).thenReturn("encoded-password");
        assignIds(departmentMapper, positionMapper, roleMapper, userMapper,
                groupMapper, permissionMapper, menuMapper);

        TenantProvisioningServiceImpl service = new TenantProvisioningServiceImpl(
                departmentMapper, positionMapper, roleMapper, userMapper,
                groupMapper, permissionMapper, menuMapper, roleRelationMapper,
                userRoleRelationMapper, userPositionRelationMapper, passwordEncoder, idGenerator);
        TenantCreateDTO dto = new TenantCreateDTO();
        dto.setAdminUsername("admin");
        dto.setAdminDisplayName("租户管理员");
        dto.setAdminPassword("Admin@123456");
        dto.setMenuIds(List.of(5L));

        service.initialize(1L, 2L, dto, 99L);

        verify(roleRelationMapper).insertPermissions(any(), any(), eq(99L));
        verify(roleRelationMapper).insertMenus(any(),
                org.mockito.ArgumentMatchers.argThat(ids -> ids.size() == 2), eq(99L));
        verify(userRoleRelationMapper).insertRoles(any(), any(), eq(99L));
        verify(userPositionRelationMapper).insertPositions(any(), any(), eq(99L));
        verify(permissionMapper, times(1)).insert(any(PermissionInfo.class));
        verify(groupMapper, times(2)).insert(any(PermissionGroupInfo.class));
        assertTrue(TenantContextHolder.currentTenantId().isEmpty());
    }

    @Test
    void shouldRejectMenuOutsideCurrentTenantTemplate() {
        when(groupMapper.selectList(any())).thenReturn(List.of());
        when(permissionMapper.selectList(any())).thenReturn(List.of());
        when(menuMapper.selectList(any())).thenReturn(List.of(menu(4L, null, "/system")));
        TenantProvisioningServiceImpl service = new TenantProvisioningServiceImpl(
                departmentMapper, positionMapper, roleMapper, userMapper,
                groupMapper, permissionMapper, menuMapper, roleRelationMapper,
                userRoleRelationMapper, userPositionRelationMapper, passwordEncoder, idGenerator);
        TenantCreateDTO dto = new TenantCreateDTO();
        dto.setMenuIds(List.of(999L));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.initialize(1L, 2L, dto, 99L));

        assertEquals("SYSTEM_015", exception.getCode());
        assertTrue(TenantContextHolder.currentTenantId().isEmpty());
    }

    @Test
    void shouldRejectPlatformMenuAssignment() {
        MenuInfo platformMenu = menu(7L, null, "/system/tenant");
        when(groupMapper.selectList(any())).thenReturn(List.of());
        when(permissionMapper.selectList(any())).thenReturn(List.of());
        when(menuMapper.selectList(any())).thenReturn(List.of(platformMenu));
        TenantProvisioningServiceImpl service = new TenantProvisioningServiceImpl(
                departmentMapper, positionMapper, roleMapper, userMapper,
                groupMapper, permissionMapper, menuMapper, roleRelationMapper,
                userRoleRelationMapper, userPositionRelationMapper, passwordEncoder, idGenerator);
        TenantCreateDTO dto = new TenantCreateDTO();
        dto.setMenuIds(List.of(7L));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.initialize(1L, 2L, dto, 99L));

        assertEquals("SYSTEM_020", exception.getCode());
        assertTrue(TenantContextHolder.currentTenantId().isEmpty());
    }

    private void assignIds(Object... mappers) {
        AtomicLong ids = new AtomicLong(100L);
        for (Object mapper : mappers) {
            doAnswer(invocation -> {
                BaseEntity entity = invocation.getArgument(0);
                assertEquals(2L, TenantContextHolder.currentTenantId().orElseThrow());
                assertNull(entity.getTenantId());
                entity.setId(ids.incrementAndGet());
                return 1;
            }).when((com.baomidou.mybatisplus.core.mapper.BaseMapper<BaseEntity>) mapper)
                    .insert(org.mockito.ArgumentMatchers.<BaseEntity>any());
        }
    }

    private PermissionGroupInfo group(Long id, Long parentId, String code) {
        PermissionGroupInfo entity = new PermissionGroupInfo();
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setGroupCode(code);
        entity.setGroupName(code);
        entity.setSortOrder(0);
        entity.setEnabled(true);
        return entity;
    }

    private PermissionInfo permission(Long id, Long groupId, String code) {
        PermissionInfo entity = new PermissionInfo();
        entity.setId(id);
        entity.setGroupId(groupId);
        entity.setPermissionCode(code);
        entity.setPermissionName(code);
        entity.setPermissionType("API");
        entity.setEnabled(true);
        return entity;
    }

    private MenuInfo menu(Long id, Long parentId, String route) {
        MenuInfo entity = new MenuInfo();
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setMenuName(route);
        entity.setRoutePath(route);
        entity.setSortOrder(0);
        entity.setEnabled(true);
        return entity;
    }
}

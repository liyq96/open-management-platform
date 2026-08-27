package com.openplatform.system.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.database.model.BaseEntity;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.database.tenant.TenantContextHolder;
import com.openplatform.system.department.mapper.DepartmentInfoMapper;
import com.openplatform.system.department.model.entity.DepartmentInfo;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.menu.mapper.MenuInfoMapper;
import com.openplatform.system.menu.converter.MenuInfoConverter;
import com.openplatform.system.menu.model.entity.MenuInfo;
import com.openplatform.system.menu.model.vo.MenuInfoVO;
import com.openplatform.system.permission.group.mapper.PermissionGroupInfoMapper;
import com.openplatform.system.permission.group.model.entity.PermissionGroupInfo;
import com.openplatform.system.permission.mapper.PermissionInfoMapper;
import com.openplatform.system.permission.model.entity.PermissionInfo;
import com.openplatform.system.position.mapper.PositionInfoMapper;
import com.openplatform.system.position.model.entity.PositionInfo;
import com.openplatform.system.role.mapper.RoleInfoMapper;
import com.openplatform.system.role.mapper.RoleRelationMapper;
import com.openplatform.system.role.model.entity.RoleInfo;
import com.openplatform.system.tenant.model.dto.TenantCreateDTO;
import com.openplatform.system.tenant.service.TenantProvisioningService;
import com.openplatform.system.tenant.security.PlatformResourcePolicy;
import com.openplatform.system.user.mapper.UserInfoMapper;
import com.openplatform.system.user.mapper.UserPositionRelationMapper;
import com.openplatform.system.user.mapper.UserRoleRelationMapper;
import com.openplatform.system.user.model.entity.UserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 以创建者当前租户的菜单和权限定义为模板，初始化一个可立即登录的新租户。
 */
@Service
@RequiredArgsConstructor
public class TenantProvisioningServiceImpl implements TenantProvisioningService {

    private final DepartmentInfoMapper departmentInfoMapper;
    private final PositionInfoMapper positionInfoMapper;
    private final RoleInfoMapper roleInfoMapper;
    private final UserInfoMapper userInfoMapper;
    private final PermissionGroupInfoMapper permissionGroupInfoMapper;
    private final PermissionInfoMapper permissionInfoMapper;
    private final MenuInfoMapper menuInfoMapper;
    private final RoleRelationMapper roleRelationMapper;
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final UserPositionRelationMapper userPositionRelationMapper;
    private final PasswordEncoder passwordEncoder;
    private final PlatformIdGenerator idGenerator;

    @Override
    public List<MenuInfoVO> menuOptions() {
        List<MenuInfo> menus = menuInfoMapper.selectList(
                new LambdaQueryWrapper<MenuInfo>()
                        .eq(MenuInfo::getEnabled, true)
                        .orderByAsc(MenuInfo::getSortOrder)
                        .orderByAsc(MenuInfo::getId));
        return MenuInfoConverter.toTree(menus.stream()
                .filter(menu -> !isPlatformMenu(menu))
                .toList());
    }

    @Override
    public void initialize(Long sourceTenantId, Long targetTenantId, TenantCreateDTO dto, Long operatorId) {
        TenantTemplate template = loadTemplate(sourceTenantId, dto.getMenuIds());
        try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(targetTenantId)) {
            DepartmentInfo rootDepartment = createRootDepartment(operatorId);
            PositionInfo superAdminPosition = createSuperAdminPosition(operatorId);
            Map<Long, Long> groupIds = clonePermissionGroups(
                    template.getPermissionGroups(), operatorId);
            List<Long> permissionIds = clonePermissions(
                    template.getPermissions(), groupIds, operatorId);
            List<Long> menuIds = cloneMenus(template.getMenus(), operatorId);
            RoleInfo superAdminRole = createSuperAdminRole(operatorId);
            UserInfo superAdministrator = createSuperAdministrator(
                    rootDepartment.getId(), dto, operatorId);

            if (!permissionIds.isEmpty()) {
                roleRelationMapper.insertPermissions(
                        superAdminRole.getId(), idGenerator.relationItems(permissionIds), operatorId);
            }
            if (!menuIds.isEmpty()) {
                roleRelationMapper.insertMenus(
                        superAdminRole.getId(), idGenerator.relationItems(menuIds), operatorId);
            }
            userRoleRelationMapper.insertRoles(
                    superAdministrator.getId(),
                    idGenerator.relationItems(List.of(superAdminRole.getId())), operatorId);
            userPositionRelationMapper.insertPositions(
                    superAdministrator.getId(),
                    idGenerator.relationItems(List.of(superAdminPosition.getId())), operatorId);
        }
    }

    private TenantTemplate loadTemplate(Long sourceTenantId, List<Long> requestedMenuIds) {
        try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(sourceTenantId)) {
            List<PermissionGroupInfo> allGroups = permissionGroupInfoMapper.selectList(
                    new LambdaQueryWrapper<PermissionGroupInfo>()
                            .orderByAsc(PermissionGroupInfo::getSortOrder)
                            .orderByAsc(PermissionGroupInfo::getId));
            List<PermissionInfo> permissions = permissionInfoMapper.selectList(
                            new LambdaQueryWrapper<PermissionInfo>().orderByAsc(PermissionInfo::getId))
                    .stream()
                    .filter(permission -> !PlatformResourcePolicy.isPlatformPermission(
                            permission.getPermissionCode()))
                    .toList();
            List<PermissionGroupInfo> groups = selectPermissionGroups(allGroups, permissions);
            List<MenuInfo> menus = menuInfoMapper.selectList(
                    new LambdaQueryWrapper<MenuInfo>()
                            .orderByAsc(MenuInfo::getSortOrder)
                            .orderByAsc(MenuInfo::getId));
            return new TenantTemplate(groups, permissions, selectMenus(menus, requestedMenuIds));
        }
    }

    private List<MenuInfo> selectMenus(List<MenuInfo> availableMenus, List<Long> requestedMenuIds) {
        Map<Long, MenuInfo> menuMap = new HashMap<>();
        for (MenuInfo menu : availableMenus) {
            menuMap.put(menu.getId(), menu);
        }
        Set<Long> selectedIds = new LinkedHashSet<>();
        for (Long requestedId : new HashSet<>(requestedMenuIds)) {
            MenuInfo menu = menuMap.get(requestedId);
            if (menu == null || !Boolean.TRUE.equals(menu.getEnabled())) {
                throw new BusinessException(SystemErrorCode.MENU_NOT_FOUND);
            }
            if (isPlatformMenu(menu)) {
                throw new BusinessException(SystemErrorCode.PLATFORM_MENU_NOT_ASSIGNABLE);
            }
            Long currentId = requestedId;
            Set<Long> visitedIds = new HashSet<>();
            while (currentId != null) {
                if (!visitedIds.add(currentId)) {
                    throw new BusinessException(SystemErrorCode.MENU_PARENT_INVALID);
                }
                MenuInfo current = menuMap.get(currentId);
                if (current == null) {
                    throw new BusinessException(SystemErrorCode.MENU_PARENT_INVALID);
                }
                selectedIds.add(currentId);
                currentId = current.getParentId();
            }
        }
        return availableMenus.stream().filter(menu -> selectedIds.contains(menu.getId())).toList();
    }

    private List<PermissionGroupInfo> selectPermissionGroups(
            List<PermissionGroupInfo> availableGroups, List<PermissionInfo> permissions) {
        Map<Long, PermissionGroupInfo> groupMap = new HashMap<>();
        availableGroups.forEach(group -> groupMap.put(group.getId(), group));
        Set<Long> selectedIds = new HashSet<>();
        for (PermissionInfo permission : permissions) {
            Long currentId = permission.getGroupId();
            while (currentId != null && selectedIds.add(currentId)) {
                PermissionGroupInfo group = groupMap.get(currentId);
                if (group == null) {
                    throw new IllegalStateException("Permission template references a missing group");
                }
                currentId = group.getParentId();
            }
        }
        return availableGroups.stream().filter(group -> selectedIds.contains(group.getId())).toList();
    }

    private boolean isPlatformMenu(MenuInfo menu) {
        return PlatformResourcePolicy.isPlatformMenu(menu.getComponentCode(), menu.getRoutePath());
    }

    private DepartmentInfo createRootDepartment(Long operatorId) {
        DepartmentInfo entity = new DepartmentInfo();
        initialize(entity, operatorId);
        entity.setParentId(null);
        entity.setDepartmentCode("HEADQUARTERS");
        entity.setDepartmentName("总部");
        entity.setSortOrder(0);
        entity.setEnabled(true);
        departmentInfoMapper.insert(entity);
        return entity;
    }

    private PositionInfo createSuperAdminPosition(Long operatorId) {
        PositionInfo entity = new PositionInfo();
        initialize(entity, operatorId);
        entity.setPositionCode("SUPER_ADMIN");
        entity.setPositionName("超级管理员");
        entity.setSortOrder(0);
        entity.setEnabled(true);
        positionInfoMapper.insert(entity);
        return entity;
    }

    private RoleInfo createSuperAdminRole(Long operatorId) {
        RoleInfo entity = new RoleInfo();
        initialize(entity, operatorId);
        entity.setRoleCode("SUPER_ADMIN");
        entity.setRoleName("超级管理员");
        entity.setEnabled(true);
        roleInfoMapper.insert(entity);
        return entity;
    }

    private UserInfo createSuperAdministrator(
            Long departmentId, TenantCreateDTO dto, Long operatorId) {
        UserInfo entity = new UserInfo();
        initialize(entity, operatorId);
        entity.setDepartmentId(departmentId);
        entity.setUsername(dto.getAdminUsername());
        entity.setPassword(passwordEncoder.encode(dto.getAdminPassword()));
        entity.setDisplayName(dto.getAdminDisplayName());
        entity.setEnabled(true);
        entity.setAuthVersion(1);
        entity.setPlatformAdmin(false);
        userInfoMapper.insert(entity);
        return entity;
    }

    private Map<Long, Long> clonePermissionGroups(
            List<PermissionGroupInfo> sources, Long operatorId) {
        Map<Long, Long> idMap = new HashMap<>();
        List<PermissionGroupInfo> remaining = new ArrayList<>(sources);
        while (!remaining.isEmpty()) {
            int before = remaining.size();
            remaining.removeIf(source -> {
                if (source.getParentId() != null && !idMap.containsKey(source.getParentId())) {
                    return false;
                }
                PermissionGroupInfo target = new PermissionGroupInfo();
                initialize(target, operatorId);
                target.setParentId(source.getParentId() == null ? null : idMap.get(source.getParentId()));
                target.setGroupCode(source.getGroupCode());
                target.setGroupName(source.getGroupName());
                target.setSortOrder(source.getSortOrder());
                target.setEnabled(source.getEnabled());
                permissionGroupInfoMapper.insert(target);
                idMap.put(source.getId(), target.getId());
                return true;
            });
            if (remaining.size() == before) {
                throw new IllegalStateException("Permission group template contains an invalid parent relationship");
            }
        }
        return idMap;
    }

    private List<Long> clonePermissions(
            List<PermissionInfo> sources, Map<Long, Long> groupIds, Long operatorId) {
        List<Long> ids = new ArrayList<>();
        for (PermissionInfo source : sources) {
            Long groupId = groupIds.get(source.getGroupId());
            if (groupId == null) {
                throw new IllegalStateException("Permission template references a missing group");
            }
            PermissionInfo target = new PermissionInfo();
            initialize(target, operatorId);
            target.setGroupId(groupId);
            target.setPermissionCode(source.getPermissionCode());
            target.setPermissionName(source.getPermissionName());
            target.setPermissionType(source.getPermissionType());
            target.setEnabled(source.getEnabled());
            permissionInfoMapper.insert(target);
            ids.add(target.getId());
        }
        return ids;
    }

    private List<Long> cloneMenus(List<MenuInfo> sources, Long operatorId) {
        Map<Long, Long> idMap = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        List<MenuInfo> remaining = new ArrayList<>(sources);
        while (!remaining.isEmpty()) {
            int before = remaining.size();
            remaining.removeIf(source -> {
                if (source.getParentId() != null && !idMap.containsKey(source.getParentId())) {
                    return false;
                }
                MenuInfo target = new MenuInfo();
                initialize(target, operatorId);
                target.setParentId(source.getParentId() == null ? null : idMap.get(source.getParentId()));
                target.setMenuName(source.getMenuName());
                target.setRoutePath(source.getRoutePath());
        target.setComponentCode(source.getComponentCode());
                target.setIcon(source.getIcon());
                target.setSortOrder(source.getSortOrder());
                target.setEnabled(source.getEnabled());
                menuInfoMapper.insert(target);
                idMap.put(source.getId(), target.getId());
                ids.add(target.getId());
                return true;
            });
            if (remaining.size() == before) {
                throw new IllegalStateException("Menu template contains an invalid parent relationship");
            }
        }
        return ids;
    }

    private void initialize(BaseEntity entity, Long operatorId) {
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setVersion(0);
        entity.setDeleted(false);
    }

    @Getter
    @RequiredArgsConstructor
    private static final class TenantTemplate {
        private final List<PermissionGroupInfo> permissionGroups;
        private final List<PermissionInfo> permissions;
        private final List<MenuInfo> menus;
    }
}

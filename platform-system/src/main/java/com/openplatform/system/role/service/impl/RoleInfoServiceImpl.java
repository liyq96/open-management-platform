package com.openplatform.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.role.converter.RoleInfoConverter;
import com.openplatform.system.role.mapper.RoleInfoMapper;
import com.openplatform.system.role.mapper.RoleRelationMapper;
import com.openplatform.system.role.model.dto.RoleCreateDTO;
import com.openplatform.system.role.model.dto.RoleDeleteDTO;
import com.openplatform.system.role.model.dto.RoleMenuAssignDTO;
import com.openplatform.system.role.model.dto.RolePermissionAssignDTO;
import com.openplatform.system.role.model.dto.RoleQueryDTO;
import com.openplatform.system.role.model.dto.RoleUpdateDTO;
import com.openplatform.system.role.model.entity.RoleInfo;
import com.openplatform.system.role.model.vo.RoleInfoVO;
import com.openplatform.system.role.service.RoleInfoService;
import com.openplatform.system.security.service.UserAccessCacheInvalidationService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 角色管理服务实现。 */
@Service
@RequiredArgsConstructor
public class RoleInfoServiceImpl implements RoleInfoService {

    private final RoleInfoMapper roleInfoMapper;
    private final RoleRelationMapper roleRelationMapper;
    private final PlatformIdGenerator idGenerator;
    private final UserAccessCacheInvalidationService cacheInvalidationService;

    @Override
    public PageResult<RoleInfoVO> page(RoleQueryDTO dto) {
        LambdaQueryWrapper<RoleInfo> wrapper = new LambdaQueryWrapper<RoleInfo>()
                .eq(dto.getEnabled() != null, RoleInfo::getEnabled, dto.getEnabled())
                .and(StringUtils.hasText(dto.getKeyword()), query -> query
                        .like(RoleInfo::getRoleCode, dto.getKeyword())
                        .or().like(RoleInfo::getRoleName, dto.getKeyword()))
                .orderByAsc(RoleInfo::getRoleCode);
        Page<RoleInfo> result = roleInfoMapper.selectPage(
                new Page<>(dto.getPage(), dto.getPageSize()), wrapper);
        List<RoleInfoVO> records = result.getRecords().stream()
                .map(entity -> RoleInfoConverter.toVO(entity, List.of()))
                .toList();
        return PageResult.of(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public RoleInfoVO detail(Long roleId) {
        return RoleInfoConverter.toVO(
                requireRole(roleId),
                roleRelationMapper.selectPermissionIds(roleId),
                roleRelationMapper.selectMenuIds(roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleCreateDTO dto) {
        validateCode(dto.getRoleCode(), null);
        Long operatorId = SecurityContextUtils.requireUserId();
        RoleInfo entity = new RoleInfo();
        entity.setRoleCode(dto.getRoleCode());
        entity.setRoleName(dto.getRoleName());
        entity.setEnabled(dto.getEnabled());
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setVersion(0);
        entity.setDeleted(false);
        roleInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleUpdateDTO dto) {
        RoleInfo entity = requireRole(dto.getRoleId());
        validateCode(dto.getRoleCode(), dto.getRoleId());
        boolean securityChanged = !entity.getEnabled().equals(dto.getEnabled());
        entity.setRoleCode(dto.getRoleCode());
        entity.setRoleName(dto.getRoleName());
        entity.setEnabled(dto.getEnabled());
        entity.setUpdatedBy(SecurityContextUtils.requireUserId());
        roleInfoMapper.updateById(entity);
        if (securityChanged) {
            roleRelationMapper.incrementUserAuthVersion(dto.getRoleId());
            cacheInvalidationService.invalidateTenantAfterCommit(SecurityContextUtils.requireTenantId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(RoleDeleteDTO dto) {
        List<Long> roleIds = dto.getRoleIds().stream().distinct().toList();
        Long count = roleInfoMapper.selectCount(new LambdaQueryWrapper<RoleInfo>()
                .in(RoleInfo::getId, roleIds));
        if (count != roleIds.size()) {
            throw new BusinessException(SystemErrorCode.ROLE_NOT_FOUND);
        }
        for (Long roleId : roleIds) {
            if (roleRelationMapper.countUsers(roleId) > 0) {
                throw new BusinessException(SystemErrorCode.ROLE_IN_USE);
            }
        }
        for (Long roleId : roleIds) {
            roleRelationMapper.deletePermissions(roleId);
            roleRelationMapper.deleteMenus(roleId);
        }
        roleInfoMapper.deleteByIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(RolePermissionAssignDTO dto) {
        requireRole(dto.getRoleId());
        Set<Long> requestedIds = new LinkedHashSet<>(dto.getPermissionIds());
        if (!requestedIds.isEmpty()
                && roleRelationMapper.countEnabledPermissions(requestedIds) != requestedIds.size()) {
            throw new BusinessException(SystemErrorCode.PERMISSION_NOT_FOUND);
        }
        if (requestedIds.equals(new LinkedHashSet<>(
                roleRelationMapper.selectPermissionIds(dto.getRoleId())))) {
            return;
        }
        roleRelationMapper.deletePermissions(dto.getRoleId());
        if (!requestedIds.isEmpty()) {
            roleRelationMapper.insertPermissions(dto.getRoleId(),
                    idGenerator.relationItems(requestedIds),
                    SecurityContextUtils.requireUserId());
        }
        roleRelationMapper.incrementUserAuthVersion(dto.getRoleId());
        cacheInvalidationService.invalidateTenantAfterCommit(SecurityContextUtils.requireTenantId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(RoleMenuAssignDTO dto) {
        requireRole(dto.getRoleId());
        Set<Long> requestedIds = new LinkedHashSet<>(dto.getMenuIds());
        if (!requestedIds.isEmpty()
                && roleRelationMapper.countEnabledMenus(requestedIds) != requestedIds.size()) {
            throw new BusinessException(SystemErrorCode.MENU_NOT_FOUND);
        }
        if (requestedIds.equals(new LinkedHashSet<>(
                roleRelationMapper.selectMenuIds(dto.getRoleId())))) {
            return;
        }
        roleRelationMapper.deleteMenus(dto.getRoleId());
        if (!requestedIds.isEmpty()) {
            roleRelationMapper.insertMenus(dto.getRoleId(),
                    idGenerator.relationItems(requestedIds),
                    SecurityContextUtils.requireUserId());
        }
        roleRelationMapper.incrementUserAuthVersion(dto.getRoleId());
        cacheInvalidationService.invalidateTenantAfterCommit(SecurityContextUtils.requireTenantId());
    }

    private void validateCode(String code, Long excludedId) {
        Long count = roleInfoMapper.selectCount(new LambdaQueryWrapper<RoleInfo>()
                .eq(RoleInfo::getRoleCode, code)
                .ne(excludedId != null, RoleInfo::getId, excludedId));
        if (count > 0) {
            throw new BusinessException(SystemErrorCode.ROLE_CODE_EXISTS);
        }
    }

    private RoleInfo requireRole(Long roleId) {
        RoleInfo role = roleInfoMapper.selectOne(new LambdaQueryWrapper<RoleInfo>()
                .eq(RoleInfo::getId, roleId));
        if (role == null) {
            throw new BusinessException(SystemErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }
}

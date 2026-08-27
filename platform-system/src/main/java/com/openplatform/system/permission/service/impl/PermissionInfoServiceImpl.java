package com.openplatform.system.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.permission.converter.PermissionInfoConverter;
import com.openplatform.system.permission.group.mapper.PermissionGroupInfoMapper;
import com.openplatform.system.permission.group.model.entity.PermissionGroupInfo;
import com.openplatform.system.permission.mapper.PermissionInfoMapper;
import com.openplatform.system.permission.mapper.PermissionReferenceMapper;
import com.openplatform.system.permission.model.dto.PermissionCreateDTO;
import com.openplatform.system.permission.model.dto.PermissionDeleteDTO;
import com.openplatform.system.permission.model.dto.PermissionQueryDTO;
import com.openplatform.system.permission.model.dto.PermissionUpdateDTO;
import com.openplatform.system.permission.model.entity.PermissionInfo;
import com.openplatform.system.permission.model.vo.PermissionInfoVO;
import com.openplatform.system.permission.service.PermissionInfoService;
import com.openplatform.system.security.service.UserAccessCacheInvalidationService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 权限标识管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class PermissionInfoServiceImpl implements PermissionInfoService {

    private final PermissionInfoMapper permissionInfoMapper;
    private final PermissionReferenceMapper permissionReferenceMapper;
    private final PermissionGroupInfoMapper permissionGroupInfoMapper;
    private final UserAccessCacheInvalidationService cacheInvalidationService;

    @Override
    public PageResult<PermissionInfoVO> page(PermissionQueryDTO dto) {
        LambdaQueryWrapper<PermissionInfo> wrapper = new LambdaQueryWrapper<PermissionInfo>()
                .eq(dto.getGroupId() != null, PermissionInfo::getGroupId, dto.getGroupId())
                .eq(dto.getPermissionType() != null,
                        PermissionInfo::getPermissionType,
                        dto.getPermissionType() == null ? null : dto.getPermissionType().name())
                .eq(dto.getEnabled() != null, PermissionInfo::getEnabled, dto.getEnabled())
                .and(StringUtils.hasText(dto.getKeyword()), query -> query
                        .like(PermissionInfo::getPermissionCode, dto.getKeyword())
                        .or().like(PermissionInfo::getPermissionName, dto.getKeyword()))
                .orderByAsc(PermissionInfo::getPermissionCode);
        Page<PermissionInfo> result = permissionInfoMapper.selectPage(
                new Page<>(dto.getPage(), dto.getPageSize()), wrapper);
        List<PermissionInfoVO> records = result.getRecords().stream()
                .map(PermissionInfoConverter::toVO)
                .toList();
        return PageResult.of(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public PermissionInfoVO detail(Long permissionId) {
        return PermissionInfoConverter.toVO(requirePermission(permissionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PermissionCreateDTO dto) {
        requireGroup(dto.getGroupId());
        validateCodeUnique(dto.getPermissionCode(), null);
        Long operatorId = SecurityContextUtils.requireUserId();
        PermissionInfo entity = new PermissionInfo();
        entity.setGroupId(dto.getGroupId());
        entity.setPermissionCode(dto.getPermissionCode());
        entity.setPermissionName(dto.getPermissionName());
        entity.setPermissionType(dto.getPermissionType().name());
        entity.setEnabled(dto.getEnabled());
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setVersion(0);
        entity.setDeleted(false);
        permissionInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PermissionUpdateDTO dto) {
        PermissionInfo entity = requirePermission(dto.getPermissionId());
        requireGroup(dto.getGroupId());
        validateCodeUnique(dto.getPermissionCode(), dto.getPermissionId());
        boolean securityChanged = !Objects.equals(entity.getPermissionCode(), dto.getPermissionCode())
                || !Objects.equals(entity.getEnabled(), dto.getEnabled());
        entity.setGroupId(dto.getGroupId());
        entity.setPermissionCode(dto.getPermissionCode());
        entity.setPermissionName(dto.getPermissionName());
        entity.setPermissionType(dto.getPermissionType().name());
        entity.setEnabled(dto.getEnabled());
        entity.setUpdatedBy(SecurityContextUtils.requireUserId());
        permissionInfoMapper.updateById(entity);
        if (securityChanged) {
            permissionReferenceMapper.incrementReferencedUserAuthVersion(dto.getPermissionId());
            cacheInvalidationService.invalidateTenantAfterCommit(SecurityContextUtils.requireTenantId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(PermissionDeleteDTO dto) {
        List<Long> permissionIds = dto.getPermissionIds().stream().distinct().toList();
        Long existingCount = permissionInfoMapper.selectCount(new LambdaQueryWrapper<PermissionInfo>()
                .in(PermissionInfo::getId, permissionIds));
        if (existingCount != permissionIds.size()) {
            throw new BusinessException(SystemErrorCode.PERMISSION_NOT_FOUND);
        }
        if (permissionReferenceMapper.countRoleReferences(permissionIds) > 0) {
            throw new BusinessException(SystemErrorCode.PERMISSION_IN_USE);
        }
        permissionInfoMapper.deleteByIds(permissionIds);
    }

    private void validateCodeUnique(String permissionCode, Long excludedId) {
        Long count = permissionInfoMapper.selectCount(new LambdaQueryWrapper<PermissionInfo>()
                .eq(PermissionInfo::getPermissionCode, permissionCode)
                .ne(excludedId != null, PermissionInfo::getId, excludedId));
        if (count > 0) {
            throw new BusinessException(SystemErrorCode.PERMISSION_CODE_EXISTS);
        }
    }

    private PermissionInfo requirePermission(Long permissionId) {
        PermissionInfo permission = permissionInfoMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(SystemErrorCode.PERMISSION_NOT_FOUND);
        }
        return permission;
    }

    private PermissionGroupInfo requireGroup(Long groupId) {
        PermissionGroupInfo group = permissionGroupInfoMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(SystemErrorCode.PERMISSION_GROUP_NOT_FOUND);
        }
        return group;
    }
}

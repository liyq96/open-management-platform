package com.openplatform.system.permission.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.permission.group.converter.PermissionGroupInfoConverter;
import com.openplatform.system.permission.group.mapper.PermissionGroupInfoMapper;
import com.openplatform.system.permission.group.model.dto.PermissionGroupCreateDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupDeleteDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupTreeQueryDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupUpdateDTO;
import com.openplatform.system.permission.group.model.entity.PermissionGroupInfo;
import com.openplatform.system.permission.group.model.vo.PermissionGroupInfoVO;
import com.openplatform.system.permission.group.service.PermissionGroupInfoService;
import com.openplatform.system.permission.mapper.PermissionInfoMapper;
import com.openplatform.system.permission.model.entity.PermissionInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限分组管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class PermissionGroupInfoServiceImpl implements PermissionGroupInfoService {

    private final PermissionGroupInfoMapper permissionGroupInfoMapper;
    private final PermissionInfoMapper permissionInfoMapper;

    @Override
    public List<PermissionGroupInfoVO> tree(PermissionGroupTreeQueryDTO dto) {
        List<PermissionGroupInfo> groups = permissionGroupInfoMapper.selectList(
                new LambdaQueryWrapper<PermissionGroupInfo>()
                        .eq(dto.getEnabled() != null, PermissionGroupInfo::getEnabled, dto.getEnabled())
                        .orderByAsc(PermissionGroupInfo::getSortOrder)
                        .orderByAsc(PermissionGroupInfo::getId));
        return PermissionGroupInfoConverter.toTree(groups);
    }

    @Override
    public PermissionGroupInfoVO detail(Long groupId) {
        return PermissionGroupInfoConverter.toVO(requireGroup(groupId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PermissionGroupCreateDTO dto) {
        validateParent(dto.getParentId());
        validateCodeUnique(dto.getGroupCode(), null);
        Long operatorId = SecurityContextUtils.requireUserId();
        PermissionGroupInfo entity = new PermissionGroupInfo();
        entity.setParentId(dto.getParentId());
        entity.setGroupCode(dto.getGroupCode());
        entity.setGroupName(dto.getGroupName());
        entity.setSortOrder(dto.getSortOrder());
        entity.setEnabled(dto.getEnabled());
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setVersion(0);
        entity.setDeleted(false);
        permissionGroupInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PermissionGroupUpdateDTO dto) {
        PermissionGroupInfo entity = requireGroup(dto.getGroupId());
        if (dto.getGroupId().equals(dto.getParentId())) {
            throw new BusinessException(SystemErrorCode.PERMISSION_GROUP_PARENT_INVALID);
        }
        validateParent(dto.getParentId());
        validateNoCycle(dto.getGroupId(), dto.getParentId());
        validateCodeUnique(dto.getGroupCode(), dto.getGroupId());
        entity.setParentId(dto.getParentId());
        entity.setGroupCode(dto.getGroupCode());
        entity.setGroupName(dto.getGroupName());
        entity.setSortOrder(dto.getSortOrder());
        entity.setEnabled(dto.getEnabled());
        entity.setUpdatedBy(SecurityContextUtils.requireUserId());
        permissionGroupInfoMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(PermissionGroupDeleteDTO dto) {
        requireGroup(dto.getGroupId());
        Long childCount = permissionGroupInfoMapper.selectCount(
                new LambdaQueryWrapper<PermissionGroupInfo>()
                        .eq(PermissionGroupInfo::getParentId, dto.getGroupId()));
        if (childCount > 0) {
            throw new BusinessException(SystemErrorCode.PERMISSION_GROUP_HAS_CHILDREN);
        }
        Long permissionCount = permissionInfoMapper.selectCount(new LambdaQueryWrapper<PermissionInfo>()
                .eq(PermissionInfo::getGroupId, dto.getGroupId()));
        if (permissionCount > 0) {
            throw new BusinessException(SystemErrorCode.PERMISSION_GROUP_IN_USE);
        }
        permissionGroupInfoMapper.deleteById(dto.getGroupId());
    }

    private void validateParent(Long parentId) {
        if (parentId != null) {
            requireGroup(parentId);
        }
    }

    private void validateNoCycle(Long groupId, Long parentId) {
        Long currentId = parentId;
        Set<Long> visitedIds = new HashSet<>();
        while (currentId != null) {
            if (groupId.equals(currentId) || !visitedIds.add(currentId)) {
                throw new BusinessException(SystemErrorCode.PERMISSION_GROUP_PARENT_INVALID);
            }
            currentId = requireGroup(currentId).getParentId();
        }
    }

    private void validateCodeUnique(String groupCode, Long excludedId) {
        Long count = permissionGroupInfoMapper.selectCount(new LambdaQueryWrapper<PermissionGroupInfo>()
                .eq(PermissionGroupInfo::getGroupCode, groupCode)
                .ne(excludedId != null, PermissionGroupInfo::getId, excludedId));
        if (count > 0) {
            throw new BusinessException(SystemErrorCode.PERMISSION_GROUP_CODE_EXISTS);
        }
    }

    private PermissionGroupInfo requireGroup(Long groupId) {
        PermissionGroupInfo group = permissionGroupInfoMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(SystemErrorCode.PERMISSION_GROUP_NOT_FOUND);
        }
        return group;
    }
}

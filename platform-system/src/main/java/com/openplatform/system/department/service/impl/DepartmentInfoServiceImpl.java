package com.openplatform.system.department.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.department.converter.DepartmentInfoConverter;
import com.openplatform.system.department.mapper.DepartmentInfoMapper;
import com.openplatform.system.department.model.dto.DepartmentCreateDTO;
import com.openplatform.system.department.model.dto.DepartmentDeleteDTO;
import com.openplatform.system.department.model.dto.DepartmentTreeQueryDTO;
import com.openplatform.system.department.model.dto.DepartmentUpdateDTO;
import com.openplatform.system.department.model.entity.DepartmentInfo;
import com.openplatform.system.department.model.vo.DepartmentInfoVO;
import com.openplatform.system.department.service.DepartmentInfoService;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.user.mapper.UserInfoMapper;
import com.openplatform.system.user.model.entity.UserInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class DepartmentInfoServiceImpl implements DepartmentInfoService {

    private final DepartmentInfoMapper departmentInfoMapper;
    private final UserInfoMapper userInfoMapper;

    @Override
    public List<DepartmentInfoVO> tree(DepartmentTreeQueryDTO dto) {
        List<DepartmentInfo> departments = departmentInfoMapper.selectList(
                new LambdaQueryWrapper<DepartmentInfo>()
                        .eq(dto.getEnabled() != null, DepartmentInfo::getEnabled, dto.getEnabled())
                        .orderByAsc(DepartmentInfo::getSortOrder)
                        .orderByAsc(DepartmentInfo::getId));
        return DepartmentInfoConverter.toTree(departments);
    }

    @Override
    public DepartmentInfoVO detail(Long departmentId) {
        return DepartmentInfoConverter.toVO(requireDepartment(departmentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DepartmentCreateDTO dto) {
        validateParent(dto.getParentId());
        validateCodeUnique(dto.getDepartmentCode(), null);

        Long operatorId = SecurityContextUtils.requireUserId();
        DepartmentInfo entity = new DepartmentInfo();
        entity.setParentId(dto.getParentId());
        entity.setDepartmentCode(dto.getDepartmentCode());
        entity.setDepartmentName(dto.getDepartmentName());
        entity.setSortOrder(dto.getSortOrder());
        entity.setEnabled(dto.getEnabled());
        entity.setVersion(0);
        entity.setDeleted(false);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        departmentInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DepartmentUpdateDTO dto) {
        DepartmentInfo entity = requireDepartment(dto.getDepartmentId());
        if (dto.getDepartmentId().equals(dto.getParentId())) {
            throw new BusinessException(SystemErrorCode.DEPARTMENT_PARENT_INVALID);
        }
        validateParent(dto.getParentId());
        validateNoCycle(dto.getDepartmentId(), dto.getParentId());
        validateCodeUnique(dto.getDepartmentCode(), dto.getDepartmentId());

        entity.setParentId(dto.getParentId());
        entity.setDepartmentCode(dto.getDepartmentCode());
        entity.setDepartmentName(dto.getDepartmentName());
        entity.setSortOrder(dto.getSortOrder());
        entity.setEnabled(dto.getEnabled());
        entity.setUpdatedBy(SecurityContextUtils.requireUserId());
        departmentInfoMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DepartmentDeleteDTO dto) {
        requireDepartment(dto.getDepartmentId());
        Long childCount = departmentInfoMapper.selectCount(new LambdaQueryWrapper<DepartmentInfo>()
                .eq(DepartmentInfo::getParentId, dto.getDepartmentId()));
        if (childCount > 0) {
            throw new BusinessException(SystemErrorCode.DEPARTMENT_HAS_CHILDREN);
        }
        Long userCount = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getDepartmentId, dto.getDepartmentId()));
        if (userCount > 0) {
            throw new BusinessException(SystemErrorCode.DEPARTMENT_HAS_USERS);
        }
        departmentInfoMapper.deleteById(dto.getDepartmentId());
    }

    private void validateParent(Long parentId) {
        if (parentId != null) {
            requireDepartment(parentId);
        }
    }

    private void validateNoCycle(Long departmentId, Long parentId) {
        Long currentId = parentId;
        Set<Long> visitedIds = new HashSet<>();
        while (currentId != null) {
            if (departmentId.equals(currentId) || !visitedIds.add(currentId)) {
                throw new BusinessException(SystemErrorCode.DEPARTMENT_PARENT_INVALID);
            }
            currentId = requireDepartment(currentId).getParentId();
        }
    }

    private void validateCodeUnique(String departmentCode, Long excludedId) {
        Long count = departmentInfoMapper.selectCount(new LambdaQueryWrapper<DepartmentInfo>()
                .eq(DepartmentInfo::getDepartmentCode, departmentCode)
                .ne(excludedId != null, DepartmentInfo::getId, excludedId));
        if (count > 0) {
            throw new BusinessException(SystemErrorCode.DEPARTMENT_CODE_EXISTS);
        }
    }

    private DepartmentInfo requireDepartment(Long departmentId) {
        DepartmentInfo department = departmentInfoMapper.selectOne(new LambdaQueryWrapper<DepartmentInfo>()
                .eq(DepartmentInfo::getId, departmentId));
        if (department == null) {
            throw new BusinessException(SystemErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return department;
    }
}

package com.openplatform.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.department.mapper.DepartmentInfoMapper;
import com.openplatform.system.department.model.entity.DepartmentInfo;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.security.service.UserAccessCacheInvalidationService;
import com.openplatform.system.user.converter.UserInfoConverter;
import com.openplatform.system.user.mapper.UserInfoMapper;
import com.openplatform.system.user.mapper.UserRoleRelationMapper;
import com.openplatform.system.user.mapper.UserPositionRelationMapper;
import com.openplatform.system.user.model.dto.UserCreateDTO;
import com.openplatform.system.user.model.dto.UserDeleteDTO;
import com.openplatform.system.user.model.dto.UserQueryDTO;
import com.openplatform.system.user.model.dto.UserUpdateDTO;
import com.openplatform.system.user.model.dto.UserRoleAssignDTO;
import com.openplatform.system.user.model.dto.UserPositionAssignDTO;
import com.openplatform.system.user.model.dto.UserPasswordChangeDTO;
import com.openplatform.system.user.model.dto.UserPasswordResetDTO;
import com.openplatform.system.user.model.entity.UserInfo;
import com.openplatform.system.user.model.vo.UserInfoVO;
import com.openplatform.system.user.service.UserInfoService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 用户管理服务实现。 */
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoMapper userInfoMapper;
    private final DepartmentInfoMapper departmentInfoMapper;
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final UserPositionRelationMapper userPositionRelationMapper;
    private final PasswordEncoder passwordEncoder;
    private final PlatformIdGenerator idGenerator;
    private final UserAccessCacheInvalidationService cacheInvalidationService;

    @Override
    public PageResult<UserInfoVO> page(UserQueryDTO dto) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<UserInfo>()
                .eq(dto.getDepartmentId() != null, UserInfo::getDepartmentId, dto.getDepartmentId())
                .eq(dto.getEnabled() != null, UserInfo::getEnabled, dto.getEnabled())
                .and(StringUtils.hasText(dto.getKeyword()), query -> query
                        .like(UserInfo::getUsername, dto.getKeyword())
                        .or().like(UserInfo::getDisplayName, dto.getKeyword()));
        wrapper.orderByDesc(UserInfo::getCreatedAt);
        Page<UserInfo> result = userInfoMapper.selectPage(new Page<>(dto.getPage(), dto.getPageSize()), wrapper);
        List<UserInfoVO> records = result.getRecords().stream().map(UserInfoConverter::toVO).toList();
        return PageResult.of(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public UserInfoVO detail(Long userId) {
        UserInfo target = requireUser(userId);
        UserInfoVO vo=UserInfoConverter.toVO(target);
        vo.setRoleIds(userRoleRelationMapper.selectRoleIds(userId));
        vo.setPositionIds(userPositionRelationMapper.selectPositionIds(userId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserCreateDTO dto) {
        Long operatorId = SecurityContextUtils.requireUserId();
        requireDepartment(dto.getDepartmentId());
        Long count = userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(SystemErrorCode.USERNAME_EXISTS);
        }
        UserInfo entity = new UserInfo();
        entity.setDepartmentId(dto.getDepartmentId());
        entity.setUsername(dto.getUsername());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setDisplayName(dto.getDisplayName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setEnabled(dto.getEnabled());
        entity.setAuthVersion(1);
        entity.setPlatformAdmin(false);
        entity.setVersion(0);
        entity.setDeleted(false);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        userInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateDTO dto) {
        UserInfo entity = requireUser(dto.getUserId());
        requireDepartment(dto.getDepartmentId());
        boolean securityChanged = !java.util.Objects.equals(entity.getEnabled(), dto.getEnabled());
        entity.setDepartmentId(dto.getDepartmentId());
        entity.setDisplayName(dto.getDisplayName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setEnabled(dto.getEnabled());
        if (securityChanged) {
            entity.setAuthVersion(entity.getAuthVersion() + 1);
        }
        entity.setUpdatedBy(SecurityContextUtils.requireUserId());
        userInfoMapper.updateById(entity);
        if (securityChanged) {
            cacheInvalidationService.invalidateUserAfterCommit(
                    SecurityContextUtils.requireTenantId(), dto.getUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UserDeleteDTO dto) {
        Long currentUserId = SecurityContextUtils.requireUserId();
        if (dto.getUserIds().contains(currentUserId)) {
            throw new BusinessException(SystemErrorCode.CANNOT_DELETE_CURRENT_USER);
        }
        List<UserInfo> users = userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>()
                .in(UserInfo::getId, dto.getUserIds()));
        if (users.size() != dto.getUserIds().stream().distinct().count()) {
            throw new BusinessException(SystemErrorCode.USER_NOT_FOUND);
        }
        for (Long userId : dto.getUserIds().stream().distinct().toList()) {
            userRoleRelationMapper.deleteRoles(userId);
            userPositionRelationMapper.deletePositions(userId);
        }
        userInfoMapper.deleteByIds(dto.getUserIds());
        Long tenantId = SecurityContextUtils.requireTenantId();
        dto.getUserIds().stream().distinct()
                .forEach(userId -> cacheInvalidationService.invalidateUserAfterCommit(tenantId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(UserRoleAssignDTO dto) {
        requireUser(dto.getUserId());
        Set<Long> requestedIds = new LinkedHashSet<>(dto.getRoleIds());
        if (!requestedIds.isEmpty()
                && userRoleRelationMapper.countEnabledRoles(requestedIds) != requestedIds.size()) {
            throw new BusinessException(SystemErrorCode.ROLE_NOT_FOUND);
        }
        if (requestedIds.equals(new LinkedHashSet<>(
                userRoleRelationMapper.selectRoleIds(dto.getUserId())))) {
            return;
        }
        userRoleRelationMapper.deleteRoles(dto.getUserId());
        Long operatorId=SecurityContextUtils.requireUserId();
        if (!requestedIds.isEmpty()) userRoleRelationMapper.insertRoles(
                dto.getUserId(), idGenerator.relationItems(requestedIds), operatorId);
        userRoleRelationMapper.incrementAuthVersion(dto.getUserId(),operatorId);
        cacheInvalidationService.invalidateUserAfterCommit(
                SecurityContextUtils.requireTenantId(), dto.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPositions(UserPositionAssignDTO dto) {
        requireUser(dto.getUserId());
        if (!dto.getPositionIds().isEmpty()
                && userPositionRelationMapper.countEnabledPositions(dto.getPositionIds())
                != dto.getPositionIds().size()) {
            throw new BusinessException(SystemErrorCode.POSITION_NOT_FOUND);
        }
        userPositionRelationMapper.deletePositions(dto.getUserId());
        if (!dto.getPositionIds().isEmpty()) {
            userPositionRelationMapper.insertPositions(dto.getUserId(),
                    idGenerator.relationItems(dto.getPositionIds()),
                    SecurityContextUtils.requireUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserPasswordChangeDTO dto) {
        Long currentUserId = SecurityContextUtils.requireUserId();
        UserInfo currentUser = requireUser(currentUserId);
        if (!passwordEncoder.matches(dto.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(SystemErrorCode.OLD_PASSWORD_INCORRECT);
        }
        validateNewPassword(dto.getNewPassword(), dto.getConfirmPassword(), currentUser.getPassword());
        updatePassword(currentUser, dto.getNewPassword(), currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(UserPasswordResetDTO dto) {
        UserInfo target = requireUser(dto.getUserId());
        validateNewPassword(dto.getNewPassword(), dto.getConfirmPassword(), target.getPassword());
        updatePassword(target, dto.getNewPassword(), SecurityContextUtils.requireUserId());
    }

    private UserInfo requireUser(Long userId) {
        UserInfo user = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getId, userId));
        if (user == null) {
            throw new BusinessException(SystemErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void requireDepartment(Long departmentId) {
        Long count = departmentInfoMapper.selectCount(new LambdaQueryWrapper<DepartmentInfo>()
                .eq(DepartmentInfo::getId, departmentId));
        if (count == 0) {
            throw new BusinessException(SystemErrorCode.DEPARTMENT_NOT_FOUND);
        }
    }

    private void validateNewPassword(String newPassword, String confirmPassword, String oldPasswordHash) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(SystemErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
        if (passwordEncoder.matches(newPassword, oldPasswordHash)) {
            throw new BusinessException(SystemErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }
    }

    private void updatePassword(UserInfo user, String newPassword, Long operatorId) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setAuthVersion(user.getAuthVersion() + 1);
        user.setUpdatedBy(operatorId);
        userInfoMapper.updateById(user);
        cacheInvalidationService.invalidateUserAfterCommit(
                SecurityContextUtils.requireTenantId(), user.getId());
    }
}

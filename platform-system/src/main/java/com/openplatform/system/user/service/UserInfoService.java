package com.openplatform.system.user.service;

import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.user.model.dto.UserCreateDTO;
import com.openplatform.system.user.model.dto.UserDeleteDTO;
import com.openplatform.system.user.model.dto.UserQueryDTO;
import com.openplatform.system.user.model.dto.UserUpdateDTO;
import com.openplatform.system.user.model.dto.UserRoleAssignDTO;
import com.openplatform.system.user.model.dto.UserPositionAssignDTO;
import com.openplatform.system.user.model.dto.UserPasswordChangeDTO;
import com.openplatform.system.user.model.dto.UserPasswordResetDTO;
import com.openplatform.system.user.model.vo.UserInfoVO;

/** 用户管理服务。 */
public interface UserInfoService {
    PageResult<UserInfoVO> page(UserQueryDTO dto);
    UserInfoVO detail(Long userId);
    Long create(UserCreateDTO dto);
    void update(UserUpdateDTO dto);
    void delete(UserDeleteDTO dto);
    void assignRoles(UserRoleAssignDTO dto);
    void assignPositions(UserPositionAssignDTO dto);
    void changePassword(UserPasswordChangeDTO dto);
    void resetPassword(UserPasswordResetDTO dto);
}

package com.openplatform.system.role.service;

import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.role.model.dto.RoleCreateDTO;
import com.openplatform.system.role.model.dto.RoleDeleteDTO;
import com.openplatform.system.role.model.dto.RolePermissionAssignDTO;
import com.openplatform.system.role.model.dto.RoleMenuAssignDTO;
import com.openplatform.system.role.model.dto.RoleQueryDTO;
import com.openplatform.system.role.model.dto.RoleUpdateDTO;
import com.openplatform.system.role.model.vo.RoleInfoVO;

/** 角色管理服务。 */
public interface RoleInfoService {
    PageResult<RoleInfoVO> page(RoleQueryDTO dto);
    RoleInfoVO detail(Long roleId);
    Long create(RoleCreateDTO dto);
    void update(RoleUpdateDTO dto);
    void delete(RoleDeleteDTO dto);
    void assignPermissions(RolePermissionAssignDTO dto);
    void assignMenus(RoleMenuAssignDTO dto);
}

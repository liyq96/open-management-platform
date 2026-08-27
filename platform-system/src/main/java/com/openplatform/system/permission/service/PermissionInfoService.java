package com.openplatform.system.permission.service;

import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.permission.model.dto.PermissionCreateDTO;
import com.openplatform.system.permission.model.dto.PermissionDeleteDTO;
import com.openplatform.system.permission.model.dto.PermissionQueryDTO;
import com.openplatform.system.permission.model.dto.PermissionUpdateDTO;
import com.openplatform.system.permission.model.vo.PermissionInfoVO;

/**
 * 权限标识管理服务。
 */
public interface PermissionInfoService {

    PageResult<PermissionInfoVO> page(PermissionQueryDTO dto);

    PermissionInfoVO detail(Long permissionId);

    Long create(PermissionCreateDTO dto);

    void update(PermissionUpdateDTO dto);

    void delete(PermissionDeleteDTO dto);
}

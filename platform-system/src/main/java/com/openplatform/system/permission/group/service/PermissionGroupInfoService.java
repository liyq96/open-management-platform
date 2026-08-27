package com.openplatform.system.permission.group.service;

import com.openplatform.system.permission.group.model.dto.PermissionGroupCreateDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupDeleteDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupTreeQueryDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupUpdateDTO;
import com.openplatform.system.permission.group.model.vo.PermissionGroupInfoVO;
import java.util.List;

/**
 * 权限分组管理服务。
 */
public interface PermissionGroupInfoService {

    List<PermissionGroupInfoVO> tree(PermissionGroupTreeQueryDTO dto);

    PermissionGroupInfoVO detail(Long groupId);

    Long create(PermissionGroupCreateDTO dto);

    void update(PermissionGroupUpdateDTO dto);

    void delete(PermissionGroupDeleteDTO dto);
}

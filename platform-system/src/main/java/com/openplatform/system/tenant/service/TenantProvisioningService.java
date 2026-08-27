package com.openplatform.system.tenant.service;

import com.openplatform.system.tenant.model.dto.TenantCreateDTO;
import com.openplatform.system.menu.model.vo.MenuInfoVO;
import java.util.List;

/** 新租户基础数据和管理员初始化。 */
public interface TenantProvisioningService {

    List<MenuInfoVO> menuOptions();

    void initialize(Long sourceTenantId, Long targetTenantId, TenantCreateDTO dto, Long operatorId);
}

package com.openplatform.system.tenant.service;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.tenant.model.dto.*;
import com.openplatform.system.tenant.model.vo.TenantInfoVO;
/** 租户管理服务。 */
public interface TenantInfoService {
    PageResult<TenantInfoVO> page(TenantQueryDTO dto);
    TenantInfoVO detail(Long tenantId);
    Long create(TenantCreateDTO dto);
    void update(TenantUpdateDTO dto);
}

package com.openplatform.system.tenant.converter;
import com.openplatform.system.tenant.model.entity.TenantInfo;
import com.openplatform.system.tenant.model.vo.TenantInfoVO;
/** 租户对象转换器。 */
public final class TenantInfoConverter { private TenantInfoConverter(){ } public static TenantInfoVO toVO(TenantInfo e){TenantInfoVO v=new TenantInfoVO();v.setTenantId(e.getId());v.setTenantCode(e.getTenantCode());v.setTenantName(e.getTenantName());v.setEnabled(e.getEnabled());v.setCreatedAt(e.getCreatedAt());return v;} }

package com.openplatform.auth.login.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 登录前从租户目录解析租户。 */
@Mapper
public interface AuthTenantMapper {

    @Select("""
            SELECT id
              FROM tenant_info
             WHERE tenant_code = #{tenantCode}
               AND enabled = TRUE
               AND deleted = FALSE
            """)
    Long selectEnabledTenantId(@Param("tenantCode") String tenantCode);
}

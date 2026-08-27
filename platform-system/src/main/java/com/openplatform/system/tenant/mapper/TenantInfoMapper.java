package com.openplatform.system.tenant.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openplatform.system.tenant.model.entity.TenantInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
/** 租户数据访问。 */
@Mapper public interface TenantInfoMapper extends BaseMapper<TenantInfo> {
    @Update("UPDATE user_info SET auth_version=auth_version+1,updated_at=CURRENT_TIMESTAMP WHERE deleted=FALSE")
    int incrementTenantUserAuthVersion();
}

package com.openplatform.system.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openplatform.system.permission.model.entity.PermissionInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限数据访问。
 */
@Mapper
public interface PermissionInfoMapper extends BaseMapper<PermissionInfo> {
}

package com.openplatform.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openplatform.system.user.model.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/** 用户数据访问。 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}

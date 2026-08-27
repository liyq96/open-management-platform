package com.openplatform.system.user.converter;

import com.openplatform.system.user.model.entity.UserInfo;
import com.openplatform.system.user.model.vo.UserInfoVO;

/** 用户对象转换器。 */
public final class UserInfoConverter {

    private UserInfoConverter() {
    }

    public static UserInfoVO toVO(UserInfo entity) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(entity.getId());
        vo.setDepartmentId(entity.getDepartmentId());
        vo.setUsername(entity.getUsername());
        vo.setDisplayName(entity.getDisplayName());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}

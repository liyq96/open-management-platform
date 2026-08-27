package com.openplatform.system.permission.converter;

import com.openplatform.system.permission.model.entity.PermissionInfo;
import com.openplatform.system.permission.model.enums.PermissionType;
import com.openplatform.system.permission.model.vo.PermissionInfoVO;

/**
 * 权限对象转换器。
 */
public final class PermissionInfoConverter {

    private PermissionInfoConverter() {
    }

    public static PermissionInfoVO toVO(PermissionInfo entity) {
        PermissionInfoVO vo = new PermissionInfoVO();
        vo.setPermissionId(entity.getId());
        vo.setGroupId(entity.getGroupId());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setPermissionName(entity.getPermissionName());
        vo.setPermissionType(PermissionType.valueOf(entity.getPermissionType()));
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}

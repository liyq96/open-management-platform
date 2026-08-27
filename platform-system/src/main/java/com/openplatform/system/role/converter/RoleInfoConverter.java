package com.openplatform.system.role.converter;

import com.openplatform.system.role.model.entity.RoleInfo;
import com.openplatform.system.role.model.vo.RoleInfoVO;
import java.util.List;

/** 角色对象转换器。 */
public final class RoleInfoConverter {
    private RoleInfoConverter() { }

    public static RoleInfoVO toVO(RoleInfo entity, List<Long> permissionIds) {
        return toVO(entity, permissionIds, List.of());
    }

    public static RoleInfoVO toVO(RoleInfo entity, List<Long> permissionIds, List<Long> menuIds) {
        RoleInfoVO vo = new RoleInfoVO();
        vo.setRoleId(entity.getId());
        vo.setRoleCode(entity.getRoleCode());
        vo.setRoleName(entity.getRoleName());
        vo.setEnabled(entity.getEnabled());
        vo.setPermissionIds(permissionIds == null ? List.of() : List.copyOf(permissionIds));
        vo.setMenuIds(menuIds == null ? List.of() : List.copyOf(menuIds));
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}

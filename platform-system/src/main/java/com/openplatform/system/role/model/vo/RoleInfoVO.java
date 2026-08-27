package com.openplatform.system.role.model.vo;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;

/** 角色信息响应。 */
@Data
public class RoleInfoVO {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Boolean enabled;
    private List<Long> permissionIds;
    private List<Long> menuIds;
    private OffsetDateTime createdAt;
}

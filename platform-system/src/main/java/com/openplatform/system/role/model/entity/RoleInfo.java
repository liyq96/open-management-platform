package com.openplatform.system.role.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openplatform.common.database.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 角色信息实体。 */
@Getter
@Setter
@TableName("role_info")
public class RoleInfo extends BaseEntity {
    private String roleCode;
    private String roleName;
    private Boolean enabled;
}

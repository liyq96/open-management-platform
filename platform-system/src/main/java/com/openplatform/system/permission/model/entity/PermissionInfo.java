package com.openplatform.system.permission.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openplatform.common.database.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限标识实体。
 */
@Getter
@Setter
@TableName("permission_info")
public class PermissionInfo extends BaseEntity {

    private Long groupId;
    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private Boolean enabled;
}

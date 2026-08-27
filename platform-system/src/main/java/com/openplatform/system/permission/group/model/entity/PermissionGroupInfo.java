package com.openplatform.system.permission.group.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openplatform.common.database.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限分组实体。权限分组与菜单结构相互独立，并按租户隔离。
 */
@Getter
@Setter
@TableName("permission_group_info")
public class PermissionGroupInfo extends BaseEntity {

    private Long parentId;
    private String groupCode;
    private String groupName;
    private Integer sortOrder;
    private Boolean enabled;
}

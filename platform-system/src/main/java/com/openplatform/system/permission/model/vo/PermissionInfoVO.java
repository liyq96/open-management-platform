package com.openplatform.system.permission.model.vo;

import com.openplatform.system.permission.model.enums.PermissionType;
import java.time.OffsetDateTime;
import lombok.Data;

/**
 * 权限信息响应。
 */
@Data
public class PermissionInfoVO {

    private Long permissionId;
    private Long groupId;
    private String permissionCode;
    private String permissionName;
    private PermissionType permissionType;
    private Boolean enabled;
    private OffsetDateTime createdAt;
}

package com.openplatform.system.permission.group.model.vo;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 权限分组响应。
 */
@Data
public class PermissionGroupInfoVO {

    private Long groupId;
    private Long parentId;
    private String groupCode;
    private String groupName;
    private Integer sortOrder;
    private Boolean enabled;
    private OffsetDateTime createdAt;
    private List<PermissionGroupInfoVO> children = new ArrayList<>();
}

package com.openplatform.system.permission.group.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 权限分组删除参数。
 */
@Data
public class PermissionGroupDeleteDTO {

    @NotNull
    @Positive
    private Long groupId;
}

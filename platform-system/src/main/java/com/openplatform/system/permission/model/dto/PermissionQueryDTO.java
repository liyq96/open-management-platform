package com.openplatform.system.permission.model.dto;

import com.openplatform.system.permission.model.enums.PermissionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 权限分页查询参数。
 */
@Data
public class PermissionQueryDTO {

    private String keyword;

    @Positive
    private Long groupId;
    private PermissionType permissionType;
    private Boolean enabled;

    @Min(1)
    private long page = 1;

    @Min(1)
    @Max(200)
    private long pageSize = 20;
}

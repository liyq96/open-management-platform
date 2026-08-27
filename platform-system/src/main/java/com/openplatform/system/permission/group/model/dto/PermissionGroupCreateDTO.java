package com.openplatform.system.permission.group.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限分组新增参数。
 */
@Data
public class PermissionGroupCreateDTO {

    @Positive
    private Long parentId;

    @NotBlank
    @Size(max = 25)
    private String groupCode;

    @NotBlank
    @Size(max = 25)
    private String groupName;

    @NotNull
    @Min(0)
    private Integer sortOrder = 0;

    @NotNull
    private Boolean enabled = true;
}

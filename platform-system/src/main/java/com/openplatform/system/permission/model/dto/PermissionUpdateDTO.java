package com.openplatform.system.permission.model.dto;

import com.openplatform.system.permission.model.enums.PermissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限修改参数。
 */
@Data
public class PermissionUpdateDTO {

    @NotNull
    @Positive
    private Long permissionId;

    @NotNull
    @Positive
    private Long groupId;

    @NotBlank
    @Size(max = 25)
    private String permissionCode;

    @NotBlank
    @Size(max = 25)
    private String permissionName;

    @NotNull
    private PermissionType permissionType;

    @NotNull
    private Boolean enabled;
}

package com.openplatform.system.role.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 角色新增参数。 */
@Data
public class RoleCreateDTO {
    @NotBlank @Size(max = 25)
    private String roleCode;
    @NotBlank @Size(max = 25) private String roleName;
    @NotNull private Boolean enabled = true;
}

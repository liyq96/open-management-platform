package com.openplatform.system.role.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 角色修改参数。 */
@Data
public class RoleUpdateDTO {
    @NotNull @Positive private Long roleId;
    @NotBlank @Size(max = 25)
    private String roleCode;
    @NotBlank @Size(max = 25) private String roleName;
    @NotNull private Boolean enabled;
}

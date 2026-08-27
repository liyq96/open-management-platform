package com.openplatform.system.role.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 角色分页查询参数。 */
@Data
public class RoleQueryDTO {
    private String keyword;
    private Boolean enabled;
    @Min(1) private long page = 1;
    @Min(1) @Max(200) private long pageSize = 20;
}

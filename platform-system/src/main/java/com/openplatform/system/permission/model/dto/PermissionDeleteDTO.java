package com.openplatform.system.permission.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Data;

/**
 * 权限批量删除参数。
 */
@Data
public class PermissionDeleteDTO {

    @NotEmpty
    private List<@Positive Long> permissionIds;
}

package com.openplatform.system.role.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Data;

/** 角色批量删除参数。 */
@Data
public class RoleDeleteDTO {
    @NotEmpty private List<@Positive Long> roleIds;
}

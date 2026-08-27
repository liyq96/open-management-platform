package com.openplatform.system.department.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 部门删除参数。
 */
@Data
public class DepartmentDeleteDTO {

    @NotNull
    @Positive
    private Long departmentId;
}

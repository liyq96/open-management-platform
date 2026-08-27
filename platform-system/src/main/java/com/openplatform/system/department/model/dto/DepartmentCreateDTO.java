package com.openplatform.system.department.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门新增参数。
 */
@Data
public class DepartmentCreateDTO {

    @Positive
    private Long parentId;

    @NotBlank
    @Size(max = 25)
    private String departmentCode;

    @NotBlank
    @Size(max = 25)
    private String departmentName;

    @Min(0)
    @NotNull
    private Integer sortOrder = 0;

    @NotNull
    private Boolean enabled = true;
}

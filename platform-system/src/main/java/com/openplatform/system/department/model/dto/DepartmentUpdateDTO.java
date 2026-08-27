package com.openplatform.system.department.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门修改参数。
 */
@Data
public class DepartmentUpdateDTO {

    @NotNull
    @Positive
    private Long departmentId;

    @Positive
    private Long parentId;

    @NotBlank
    @Size(max = 25)
    private String departmentCode;

    @NotBlank
    @Size(max = 25)
    private String departmentName;

    @NotNull
    @Min(0)
    private Integer sortOrder;

    @NotNull
    private Boolean enabled;
}

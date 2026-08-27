package com.openplatform.system.user.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 用户分页查询参数。 */
@Data
public class UserQueryDTO {
    private String keyword;
    private Long departmentId;
    private Boolean enabled;
    @Min(1) private long page = 1;
    @Min(1) @Max(200) private long pageSize = 20;
}

package com.openplatform.system.tenant.model.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
/** 租户分页查询参数。 */
@Data
public class TenantQueryDTO {
    private String keyword;
    private Boolean enabled;
    @Min(1) private long page = 1;
    @Min(1) @Max(200) private long pageSize = 20;
}

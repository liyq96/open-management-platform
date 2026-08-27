package com.openplatform.system.tenant.model.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
/** 租户修改参数。 */
@Data
public class TenantUpdateDTO {
    @NotNull @Positive private Long tenantId;
    @NotBlank @Size(max = 25)
    private String tenantCode;
    @NotBlank @Size(max = 25) private String tenantName;
    @NotNull private Boolean enabled;
}

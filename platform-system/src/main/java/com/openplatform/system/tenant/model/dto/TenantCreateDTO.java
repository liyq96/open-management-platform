package com.openplatform.system.tenant.model.dto;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;
import lombok.ToString;
/** 租户新增参数。 */
@Data
public class TenantCreateDTO {
    @NotBlank @Size(max = 25)
    private String tenantCode;
    @NotBlank @Size(max = 25) private String tenantName;
    @NotNull private Boolean enabled = true;
    @NotBlank @Size(max = 25) private String adminUsername;
    @NotBlank @Size(max = 25) private String adminDisplayName;
    @NotBlank @Size(min = 8, max = 32) @ToString.Exclude private String adminPassword;
    @NotBlank @Size(min = 8, max = 32) @ToString.Exclude private String adminConfirmPassword;
    @NotEmpty private List<@NotNull Long> menuIds;
}

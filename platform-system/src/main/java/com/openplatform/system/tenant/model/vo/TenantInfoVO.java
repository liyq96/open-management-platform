package com.openplatform.system.tenant.model.vo;
import java.time.OffsetDateTime;
import lombok.Data;
/** 租户信息响应。 */
@Data
public class TenantInfoVO {
    private Long tenantId;
    private String tenantCode;
    private String tenantName;
    private Boolean enabled;
    private OffsetDateTime createdAt;
}

package com.openplatform.system.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** 租户信息实体。 */
@Getter
@Setter
@TableName("tenant_info")
public class TenantInfo {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private String tenantCode;
    private String tenantName;
    private Boolean enabled;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdAt;
    private Long updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedAt;
    @Version private Integer version;
    @TableLogic private Boolean deleted;
}

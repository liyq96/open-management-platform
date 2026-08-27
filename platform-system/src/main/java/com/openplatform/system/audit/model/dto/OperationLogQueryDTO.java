package com.openplatform.system.audit.model.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
/** 操作日志查询参数。 */
@Data
public class OperationLogQueryDTO {
    @Size(max = 25) private String moduleName;
    private Boolean success;
    @Min(1) private long page = 1;
    @Min(1) @Max(200) private long pageSize = 20;
}

package com.openplatform.system.audit.model.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
/** 登录日志查询参数。 */
@Data
public class LoginLogQueryDTO {
    @Size(max = 25) private String username;
    private Boolean success;
    @Min(1) private long page = 1;
    @Min(1) @Max(200) private long pageSize = 20;
}

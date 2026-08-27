package com.openplatform.system.audit.controller;
import com.openplatform.common.core.model.*;
import com.openplatform.system.audit.model.dto.*;
import com.openplatform.system.audit.model.vo.*;
import com.openplatform.system.audit.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/** 审计日志查询接口。 */
@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/system/audit")
public class AuditLogController { private final AuditLogService service;
    @PostMapping("/login/page") @PreAuthorize("hasAuthority('system:audit:login-list')") public ApiResponse<PageResult<LoginLogVO>> loginPage(@Valid @RequestBody LoginLogQueryDTO dto){return ApiResponse.success(service.loginPage(dto));}
    @PostMapping("/operation/page") @PreAuthorize("hasAuthority('system:audit:operation')") public ApiResponse<PageResult<OperationLogVO>> operationPage(@Valid @RequestBody OperationLogQueryDTO dto){return ApiResponse.success(service.operationPage(dto));}
}

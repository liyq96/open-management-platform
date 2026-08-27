package com.openplatform.system.tenant.controller;
import com.openplatform.common.core.model.*;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.menu.model.vo.MenuInfoVO;
import com.openplatform.system.tenant.model.dto.*;
import com.openplatform.system.tenant.model.vo.TenantInfoVO;
import com.openplatform.system.tenant.service.TenantInfoService;
import com.openplatform.system.tenant.service.TenantProvisioningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/** 租户管理接口。 */
@Validated @RestController @RequiredArgsConstructor @RequestMapping("/api/v1/system/tenant")
public class TenantInfoController { private final TenantInfoService service; private final TenantProvisioningService provisioningService;
    @PostMapping("/page") @PreAuthorize("hasAuthority('system:tenant:list') and @platformResourcePolicy.isPlatformAdmin()") public ApiResponse<PageResult<TenantInfoVO>> page(@Valid @RequestBody TenantQueryDTO dto){return ApiResponse.success(service.page(dto));}
    @GetMapping("/detail") @PreAuthorize("hasAuthority('system:tenant:list') and @platformResourcePolicy.isPlatformAdmin()") public ApiResponse<TenantInfoVO> detail(@NotNull @RequestParam Long tenantId){return ApiResponse.success(service.detail(tenantId));}
    @GetMapping("/menu-options") @PreAuthorize("hasAuthority('system:tenant:create') and @platformResourcePolicy.isPlatformAdmin()") public ApiResponse<List<MenuInfoVO>> menuOptions(){return ApiResponse.success(provisioningService.menuOptions());}
    @PostMapping("/create") @PreAuthorize("hasAuthority('system:tenant:create') and @platformResourcePolicy.isPlatformAdmin()") @OperationAudit(module="TENANT",operation="CREATE") public ApiResponse<Long> create(@Valid @RequestBody TenantCreateDTO dto){return ApiResponse.success(service.create(dto));}
    @PostMapping("/update") @PreAuthorize("hasAuthority('system:tenant:update') and @platformResourcePolicy.isPlatformAdmin()") @OperationAudit(module="TENANT",operation="UPDATE") public ApiResponse<Void> update(@Valid @RequestBody TenantUpdateDTO dto){service.update(dto);return ApiResponse.success(null);}
}

package com.openplatform.system.permission.controller;

import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.permission.model.dto.PermissionCreateDTO;
import com.openplatform.system.permission.model.dto.PermissionDeleteDTO;
import com.openplatform.system.permission.model.dto.PermissionQueryDTO;
import com.openplatform.system.permission.model.dto.PermissionUpdateDTO;
import com.openplatform.system.permission.model.vo.PermissionInfoVO;
import com.openplatform.system.permission.service.PermissionInfoService;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.security.service.UserPermissionService;
import com.openplatform.system.tenant.security.PlatformResourcePolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限标识管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/permission")
public class PermissionInfoController {

    private final PermissionInfoService permissionInfoService;

    private final UserPermissionService userPermissionService;

    @GetMapping("/current")
    public ApiResponse<Set<String>> current() {
        var loginUser = SecurityContextUtils.getLoginUser()
                .orElseThrow(() -> new IllegalStateException("Authenticated user is unavailable"));
        Set<String> permissions = userPermissionService.loadPermissions(
                loginUser.getUserId(), loginUser.getTenantId(), loginUser.getAuthVersion());
        if (!Boolean.TRUE.equals(loginUser.getPlatformAdmin())) {
            permissions = permissions.stream()
                    .filter(permission -> !PlatformResourcePolicy.isPlatformPermission(permission))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
        return ApiResponse.success(permissions);
    }

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public ApiResponse<PageResult<PermissionInfoVO>> page(@Valid @RequestBody PermissionQueryDTO dto) {
        return ApiResponse.success(permissionInfoService.page(dto));
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public ApiResponse<PermissionInfoVO> detail(@NotNull @RequestParam Long permissionId) {
        return ApiResponse.success(permissionInfoService.detail(permissionId));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:permission:create')")
    @OperationAudit(module = "PERMISSION", operation = "CREATE")
    public ApiResponse<Long> create(@Valid @RequestBody PermissionCreateDTO dto) {
        return ApiResponse.success(permissionInfoService.create(dto));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:permission:update')")
    @OperationAudit(module = "PERMISSION", operation = "UPDATE")
    public ApiResponse<Void> update(@Valid @RequestBody PermissionUpdateDTO dto) {
        permissionInfoService.update(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    @OperationAudit(module = "PERMISSION", operation = "DELETE")
    public ApiResponse<Void> delete(@Valid @RequestBody PermissionDeleteDTO dto) {
        permissionInfoService.delete(dto);
        return ApiResponse.success(null);
    }
}

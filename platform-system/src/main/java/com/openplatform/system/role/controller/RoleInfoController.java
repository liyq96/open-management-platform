package com.openplatform.system.role.controller;

import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.role.model.dto.RoleCreateDTO;
import com.openplatform.system.role.model.dto.RoleDeleteDTO;
import com.openplatform.system.role.model.dto.RoleMenuAssignDTO;
import com.openplatform.system.role.model.dto.RolePermissionAssignDTO;
import com.openplatform.system.role.model.dto.RoleQueryDTO;
import com.openplatform.system.role.model.dto.RoleUpdateDTO;
import com.openplatform.system.role.model.vo.RoleInfoVO;
import com.openplatform.system.role.service.RoleInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 角色管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/role")
public class RoleInfoController {

    private final RoleInfoService roleInfoService;

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<PageResult<RoleInfoVO>> page(@Valid @RequestBody RoleQueryDTO dto) {
        return ApiResponse.success(roleInfoService.page(dto));
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<RoleInfoVO> detail(@NotNull @RequestParam Long roleId) {
        return ApiResponse.success(roleInfoService.detail(roleId));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:role:create')")
    @OperationAudit(module = "ROLE", operation = "CREATE")
    public ApiResponse<Long> create(@Valid @RequestBody RoleCreateDTO dto) {
        return ApiResponse.success(roleInfoService.create(dto));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:role:update')")
    @OperationAudit(module = "ROLE", operation = "UPDATE")
    public ApiResponse<Void> update(@Valid @RequestBody RoleUpdateDTO dto) {
        roleInfoService.update(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @OperationAudit(module = "ROLE", operation = "DELETE")
    public ApiResponse<Void> delete(@Valid @RequestBody RoleDeleteDTO dto) {
        roleInfoService.delete(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/assign-permissions")
    @PreAuthorize("hasAuthority('system:role:permission')")
    @OperationAudit(module = "ROLE", operation = "ASSIGN_PERMISSIONS")
    public ApiResponse<Void> assignPermissions(@Valid @RequestBody RolePermissionAssignDTO dto) {
        roleInfoService.assignPermissions(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/assign-menus")
    @PreAuthorize("hasAuthority('system:role:assign-menu')")
    @OperationAudit(module = "ROLE", operation = "ASSIGN_MENUS")
    public ApiResponse<Void> assignMenus(@Valid @RequestBody RoleMenuAssignDTO dto) {
        roleInfoService.assignMenus(dto);
        return ApiResponse.success(null);
    }
}

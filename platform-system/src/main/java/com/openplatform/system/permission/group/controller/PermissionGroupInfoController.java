package com.openplatform.system.permission.group.controller;

import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.permission.group.model.dto.PermissionGroupCreateDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupDeleteDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupTreeQueryDTO;
import com.openplatform.system.permission.group.model.dto.PermissionGroupUpdateDTO;
import com.openplatform.system.permission.group.model.vo.PermissionGroupInfoVO;
import com.openplatform.system.permission.group.service.PermissionGroupInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限分组管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/permission-group")
public class PermissionGroupInfoController {

    private final PermissionGroupInfoService permissionGroupInfoService;

    @PostMapping("/tree")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public ApiResponse<List<PermissionGroupInfoVO>> tree(
            @Valid @RequestBody PermissionGroupTreeQueryDTO dto) {
        return ApiResponse.success(permissionGroupInfoService.tree(dto));
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public ApiResponse<PermissionGroupInfoVO> detail(@NotNull @RequestParam Long groupId) {
        return ApiResponse.success(permissionGroupInfoService.detail(groupId));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:permission:create')")
    @OperationAudit(module = "PERMISSION_GROUP", operation = "CREATE")
    public ApiResponse<Long> create(@Valid @RequestBody PermissionGroupCreateDTO dto) {
        return ApiResponse.success(permissionGroupInfoService.create(dto));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:permission:update')")
    @OperationAudit(module = "PERMISSION_GROUP", operation = "UPDATE")
    public ApiResponse<Void> update(@Valid @RequestBody PermissionGroupUpdateDTO dto) {
        permissionGroupInfoService.update(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    @OperationAudit(module = "PERMISSION_GROUP", operation = "DELETE")
    public ApiResponse<Void> delete(@Valid @RequestBody PermissionGroupDeleteDTO dto) {
        permissionGroupInfoService.delete(dto);
        return ApiResponse.success(null);
    }
}

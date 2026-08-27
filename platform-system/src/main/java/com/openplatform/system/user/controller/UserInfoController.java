package com.openplatform.system.user.controller;

import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.user.model.dto.UserCreateDTO;
import com.openplatform.system.user.model.dto.UserDeleteDTO;
import com.openplatform.system.user.model.dto.UserQueryDTO;
import com.openplatform.system.user.model.dto.UserUpdateDTO;
import com.openplatform.system.user.model.dto.UserRoleAssignDTO;
import com.openplatform.system.user.model.dto.UserPositionAssignDTO;
import com.openplatform.system.user.model.dto.UserPasswordChangeDTO;
import com.openplatform.system.user.model.dto.UserPasswordResetDTO;
import com.openplatform.system.user.model.vo.UserInfoVO;
import com.openplatform.system.user.service.UserInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 用户管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/user")
public class UserInfoController {

    private final UserInfoService userInfoService;

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<PageResult<UserInfoVO>> page(@Valid @RequestBody UserQueryDTO dto) {
        return ApiResponse.success(userInfoService.page(dto));
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<UserInfoVO> detail(@NotNull @RequestParam Long userId) {
        return ApiResponse.success(userInfoService.detail(userId));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:user:create')")
    @OperationAudit(module = "USER", operation = "CREATE")
    public ApiResponse<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        return ApiResponse.success(userInfoService.create(dto));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:user:update')")
    @OperationAudit(module = "USER", operation = "UPDATE")
    public ApiResponse<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        userInfoService.update(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @OperationAudit(module = "USER", operation = "DELETE")
    public ApiResponse<Void> delete(@Valid @RequestBody UserDeleteDTO dto) {
        userInfoService.delete(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/assign-roles")
    @PreAuthorize("hasAuthority('system:user:assign-role')")
    @OperationAudit(module = "USER", operation = "ASSIGN_ROLES")
    public ApiResponse<Void> assignRoles(@Valid @RequestBody UserRoleAssignDTO dto) {
        userInfoService.assignRoles(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/assign-positions")
    @PreAuthorize("hasAuthority('system:user:position')")
    @OperationAudit(module = "USER", operation = "ASSIGN_POSITIONS")
    public ApiResponse<Void> assignPositions(@Valid @RequestBody UserPositionAssignDTO dto) {
        userInfoService.assignPositions(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/change-password")
    @OperationAudit(module = "USER", operation = "CHANGE_PASSWORD")
    public ApiResponse<Void> changePassword(@Valid @RequestBody UserPasswordChangeDTO dto) {
        userInfoService.changePassword(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasAuthority('system:user:reset-pwd')")
    @OperationAudit(module = "USER", operation = "RESET_PASSWORD")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody UserPasswordResetDTO dto) {
        userInfoService.resetPassword(dto);
        return ApiResponse.success(null);
    }
}

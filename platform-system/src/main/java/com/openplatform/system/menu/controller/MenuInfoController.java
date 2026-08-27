package com.openplatform.system.menu.controller;

import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.menu.model.dto.MenuCreateDTO;
import com.openplatform.system.menu.model.dto.MenuDeleteDTO;
import com.openplatform.system.menu.model.dto.MenuTreeQueryDTO;
import com.openplatform.system.menu.model.dto.MenuUpdateDTO;
import com.openplatform.system.menu.model.vo.MenuInfoVO;
import com.openplatform.system.menu.service.MenuInfoService;
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

/** 菜单管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/menu")
public class MenuInfoController {

    private final MenuInfoService menuInfoService;

    @PostMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public ApiResponse<List<MenuInfoVO>> tree(@Valid @RequestBody MenuTreeQueryDTO dto) {
        return ApiResponse.success(menuInfoService.tree(dto));
    }

    @GetMapping("/current")
    public ApiResponse<List<MenuInfoVO>> current() {
        return ApiResponse.success(menuInfoService.currentUserTree());
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public ApiResponse<MenuInfoVO> detail(@NotNull @RequestParam Long menuId) {
        return ApiResponse.success(menuInfoService.detail(menuId));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:menu:create')")
    @OperationAudit(module = "MENU", operation = "CREATE")
    public ApiResponse<Long> create(@Valid @RequestBody MenuCreateDTO dto) {
        return ApiResponse.success(menuInfoService.create(dto));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:menu:update')")
    @OperationAudit(module = "MENU", operation = "UPDATE")
    public ApiResponse<Void> update(@Valid @RequestBody MenuUpdateDTO dto) {
        menuInfoService.update(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @OperationAudit(module = "MENU", operation = "DELETE")
    public ApiResponse<Void> delete(@Valid @RequestBody MenuDeleteDTO dto) {
        menuInfoService.delete(dto);
        return ApiResponse.success(null);
    }
}

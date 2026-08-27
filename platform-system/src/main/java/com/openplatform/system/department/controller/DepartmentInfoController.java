package com.openplatform.system.department.controller;

import com.openplatform.common.core.model.ApiResponse;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.department.model.dto.DepartmentCreateDTO;
import com.openplatform.system.department.model.dto.DepartmentDeleteDTO;
import com.openplatform.system.department.model.dto.DepartmentTreeQueryDTO;
import com.openplatform.system.department.model.dto.DepartmentUpdateDTO;
import com.openplatform.system.department.model.vo.DepartmentInfoVO;
import com.openplatform.system.department.service.DepartmentInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/department")
public class DepartmentInfoController {

    private final DepartmentInfoService departmentInfoService;

    @PostMapping("/tree")
    @PreAuthorize("hasAuthority('system:department:list')")
    public ApiResponse<List<DepartmentInfoVO>> tree(@Valid @RequestBody DepartmentTreeQueryDTO dto) {
        return ApiResponse.success(departmentInfoService.tree(dto));
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('system:department:list')")
    public ApiResponse<DepartmentInfoVO> detail(@NotNull @RequestParam Long departmentId) {
        return ApiResponse.success(departmentInfoService.detail(departmentId));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:department:create')")
    @OperationAudit(module = "DEPARTMENT", operation = "CREATE")
    public ApiResponse<Long> create(@Valid @RequestBody DepartmentCreateDTO dto) {
        return ApiResponse.success(departmentInfoService.create(dto));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:department:update')")
    @OperationAudit(module = "DEPARTMENT", operation = "UPDATE")
    public ApiResponse<Void> update(@Valid @RequestBody DepartmentUpdateDTO dto) {
        departmentInfoService.update(dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:department:delete')")
    @OperationAudit(module = "DEPARTMENT", operation = "DELETE")
    public ApiResponse<Void> delete(@Valid @RequestBody DepartmentDeleteDTO dto) {
        departmentInfoService.delete(dto);
        return ApiResponse.success(null);
    }
}

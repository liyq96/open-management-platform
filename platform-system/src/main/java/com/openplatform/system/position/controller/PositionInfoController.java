package com.openplatform.system.position.controller;
import com.openplatform.common.core.model.*;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.position.model.dto.*;
import com.openplatform.system.position.model.vo.PositionInfoVO;
import com.openplatform.system.position.service.PositionInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/** 岗位管理接口。 */
@Validated @RestController @RequiredArgsConstructor @RequestMapping("/api/v1/system/position")
public class PositionInfoController { private final PositionInfoService service;
    @PostMapping("/page") @PreAuthorize("hasAuthority('system:position:list')") public ApiResponse<PageResult<PositionInfoVO>> page(@Valid @RequestBody PositionQueryDTO dto){return ApiResponse.success(service.page(dto));}
    @GetMapping("/detail") @PreAuthorize("hasAuthority('system:position:list')") public ApiResponse<PositionInfoVO> detail(@NotNull @RequestParam Long positionId){return ApiResponse.success(service.detail(positionId));}
    @PostMapping("/create") @PreAuthorize("hasAuthority('system:position:create')") @OperationAudit(module="POSITION",operation="CREATE") public ApiResponse<Long> create(@Valid @RequestBody PositionCreateDTO dto){return ApiResponse.success(service.create(dto));}
    @PostMapping("/update") @PreAuthorize("hasAuthority('system:position:update')") @OperationAudit(module="POSITION",operation="UPDATE") public ApiResponse<Void> update(@Valid @RequestBody PositionUpdateDTO dto){service.update(dto);return ApiResponse.success(null);}
    @PostMapping("/delete") @PreAuthorize("hasAuthority('system:position:delete')") @OperationAudit(module="POSITION",operation="DELETE") public ApiResponse<Void> delete(@Valid @RequestBody PositionDeleteDTO dto){service.delete(dto);return ApiResponse.success(null);}
}

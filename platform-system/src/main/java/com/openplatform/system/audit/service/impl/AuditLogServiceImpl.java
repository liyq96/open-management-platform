package com.openplatform.system.audit.service.impl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.audit.mapper.AuditLogMapper;
import com.openplatform.system.audit.model.dto.*;
import com.openplatform.system.audit.model.vo.*;
import com.openplatform.system.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
/** 审计日志查询服务实现。 */
@Service @RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogMapper mapper;
    @Override public PageResult<LoginLogVO> loginPage(LoginLogQueryDTO dto){var result=mapper.selectLoginPage(new Page<>(dto.getPage(),dto.getPageSize()),dto.getUsername(),dto.getSuccess());return PageResult.of(result.getRecords(),result.getCurrent(),result.getSize(),result.getTotal());}
    @Override public PageResult<OperationLogVO> operationPage(OperationLogQueryDTO dto){var result=mapper.selectOperationPage(new Page<>(dto.getPage(),dto.getPageSize()),dto.getModuleName(),dto.getSuccess());return PageResult.of(result.getRecords(),result.getCurrent(),result.getSize(),result.getTotal());}
}

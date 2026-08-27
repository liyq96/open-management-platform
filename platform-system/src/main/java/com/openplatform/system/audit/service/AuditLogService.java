package com.openplatform.system.audit.service;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.audit.model.dto.*;
import com.openplatform.system.audit.model.vo.*;
/** 审计日志查询服务。 */
public interface AuditLogService { PageResult<LoginLogVO> loginPage(LoginLogQueryDTO dto); PageResult<OperationLogVO> operationPage(OperationLogQueryDTO dto); }

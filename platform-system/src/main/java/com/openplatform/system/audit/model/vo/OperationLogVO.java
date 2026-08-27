package com.openplatform.system.audit.model.vo;
import java.time.OffsetDateTime;
import lombok.Data;
/** 操作日志响应。 */
@Data public class OperationLogVO { private Long logId; private Long userId; private String moduleName; private String operationName; private String requestId; private String requestPath; private Boolean success; private OffsetDateTime createdAt; }

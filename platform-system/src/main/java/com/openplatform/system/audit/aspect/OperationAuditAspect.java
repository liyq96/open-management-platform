package com.openplatform.system.audit.aspect;

import com.openplatform.common.core.constant.CommonConstants;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.audit.annotation.OperationAudit;
import com.openplatform.system.audit.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 操作审计切面。 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationAuditAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationAuditAspect.class);
    private final AuditLogMapper auditLogMapper;
    private final PlatformIdGenerator idGenerator;
    private final HttpServletRequest request;

    @Around("@annotation(operationAudit)")
    public Object audit(ProceedingJoinPoint joinPoint, OperationAudit operationAudit) throws Throwable {
        boolean success = false;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } finally {
            writeLog(operationAudit, success);
        }
    }

    private void writeLog(OperationAudit audit, boolean success) {
        try {
            auditLogMapper.insertOperation(
                    idGenerator.nextId(),
                    SecurityContextUtils.requireUserId(),
                    audit.module(),
                    audit.operation(),
                    requestId(),
                    limit(request.getRequestURI(), 512),
                    success);
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to write operation audit log", exception);
        }
    }

    private String requestId() {
        Object value = request.getAttribute(CommonConstants.REQUEST_ID_ATTRIBUTE);
        return value == null ? null : limit(value.toString(), 128);
    }

    private String limit(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}

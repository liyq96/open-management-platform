package com.openplatform.system.audit.model.vo;
import java.time.OffsetDateTime;
import lombok.Data;
/** 登录日志响应。 */
@Data public class LoginLogVO { private Long logId; private String username; private String loginIp; private String userAgent; private Boolean success; private String failureReason; private OffsetDateTime createdAt; }

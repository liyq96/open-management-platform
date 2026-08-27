package com.openplatform.auth.login.model.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 认证服务所需的用户账号字段。
 */
@Getter
@Setter
public class AuthUserInfo {

    private Long id;

    private Long tenantId;

    private Long departmentId;

    private String username;

    @ToString.Exclude
    private String password;

    private Boolean enabled;

    private Integer authVersion;

    private Boolean platformAdmin;
}

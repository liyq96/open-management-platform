package com.openplatform.common.security.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 当前登录用户的可信身份信息。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long userId;

    private String username;

    private Long tenantId;

    private Long departmentId;

    private Integer authVersion;

    private Boolean platformAdmin;
}

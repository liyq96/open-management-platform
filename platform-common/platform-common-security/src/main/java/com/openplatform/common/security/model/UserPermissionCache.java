package com.openplatform.common.security.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 用户权限缓存。 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserPermissionCache implements Serializable {
    private Long userId;
    private Long tenantId;
    private Integer authVersion;
    private Set<String> permissions = new LinkedHashSet<>();
}

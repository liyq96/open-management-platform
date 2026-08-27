package com.openplatform.system.user.model.vo;

import java.time.OffsetDateTime;
import lombok.Data;
import java.util.List;

/** 用户信息响应。 */
@Data
public class UserInfoVO {
    private Long userId;
    private Long departmentId;
    private String username;
    private String displayName;
    private String email;
    private String phone;
    private Boolean enabled;
    private OffsetDateTime createdAt;
    private List<Long> roleIds;
    private List<Long> positionIds;
}

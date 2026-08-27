package com.openplatform.system.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openplatform.common.database.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户信息实体。
 */
@Getter
@Setter
@TableName("user_info")
public class UserInfo extends BaseEntity {

    private Long departmentId;
    private String username;

    @ToString.Exclude
    private String password;

    private String displayName;
    private String email;
    private String phone;
    private Boolean enabled;
    private Integer authVersion;
    private Boolean platformAdmin;
}

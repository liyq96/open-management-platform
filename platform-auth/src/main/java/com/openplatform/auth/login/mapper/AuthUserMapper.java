package com.openplatform.auth.login.mapper;

import com.openplatform.auth.login.model.entity.AuthUserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 登录账号查询。
 */
@Mapper
public interface AuthUserMapper {

    @Select("""
            SELECT u.id,
                   u.tenant_id,
                   u.department_id,
                   u.username,
                   u.password,
                   u.enabled,
                   u.auth_version,
                   u.platform_admin
              FROM user_info u
             JOIN tenant_info t ON t.id = u.tenant_id
             WHERE u.username = #{username}
               AND u.deleted = FALSE
               AND t.enabled = TRUE
               AND t.deleted = FALSE
             LIMIT 1
            """)
    AuthUserInfo selectByUsername(@Param("username") String username);

    @Select("""
            SELECT DISTINCT p.permission_code FROM user_role_relation ur
            JOIN role_info r ON r.id=ur.role_id AND r.enabled=TRUE AND r.deleted=FALSE
            JOIN role_permission_relation rp ON rp.role_id=r.id
            JOIN permission_info p ON p.id=rp.permission_id AND p.enabled=TRUE AND p.deleted=FALSE
            WHERE ur.user_id=#{userId} ORDER BY p.permission_code
            """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);
}

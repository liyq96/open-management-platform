package com.openplatform.system.security.mapper;

import java.util.List;
import com.openplatform.system.menu.model.entity.MenuInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 当前用户访问权限查询。 */
@Mapper
public interface UserAccessMapper {
    @Select("""
            SELECT u.auth_version FROM user_info u
            JOIN tenant_info t ON t.id=u.tenant_id AND t.enabled=TRUE AND t.deleted=FALSE
            WHERE u.id=#{userId} AND u.enabled=TRUE AND u.deleted=FALSE
            """)
    Integer selectAuthVersion(@Param("userId") Long userId);

    @Select("""
        SELECT DISTINCT p.permission_code FROM user_role_relation ur
        JOIN role_info r ON r.id=ur.role_id AND r.enabled=TRUE AND r.deleted=FALSE
        JOIN role_permission_relation rp ON rp.role_id=r.id
        JOIN permission_info p ON p.id=rp.permission_id AND p.enabled=TRUE AND p.deleted=FALSE
        WHERE ur.user_id=#{userId} ORDER BY p.permission_code
        """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);

    @Select("""
        SELECT DISTINCT m.* FROM user_role_relation ur
        JOIN role_info r ON r.id=ur.role_id AND r.enabled=TRUE AND r.deleted=FALSE
        JOIN role_menu_relation rm ON rm.role_id=r.id
        JOIN menu_info m ON m.id=rm.menu_id AND m.enabled=TRUE AND m.deleted=FALSE
        WHERE ur.user_id=#{userId}
        ORDER BY m.sort_order,m.id
        """)
    List<MenuInfo> selectMenus(@Param("userId") Long userId);
}

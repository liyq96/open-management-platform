package com.openplatform.system.permission.mapper;

import java.util.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 权限关联关系查询。
 */
@Mapper
public interface PermissionReferenceMapper {

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM role_permission_relation
            WHERE permission_id IN
            <foreach collection="permissionIds" item="permissionId" open="(" separator="," close=")">
                #{permissionId}
            </foreach>
            </script>
            """)
    long countRoleReferences(@Param("permissionIds") Collection<Long> permissionIds);

    @Update("""
            UPDATE user_info u SET auth_version=auth_version+1,updated_at=CURRENT_TIMESTAMP
            WHERE u.deleted=FALSE AND EXISTS (
              SELECT 1 FROM user_role_relation ur JOIN role_permission_relation rp
              ON rp.role_id=ur.role_id
              WHERE ur.user_id=u.id AND rp.permission_id=#{permissionId})
            """)
    int incrementReferencedUserAuthVersion(@Param("permissionId") Long permissionId);
}

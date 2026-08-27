package com.openplatform.system.role.mapper;

import com.openplatform.common.database.id.RelationInsertItem;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** 角色关联关系数据访问。 */
@Mapper
public interface RoleRelationMapper {

    @Select("SELECT permission_id FROM role_permission_relation WHERE role_id=#{roleId} ORDER BY permission_id")
    List<Long> selectPermissionIds(@Param("roleId") Long roleId);

    @Select("""
            <script>SELECT COUNT(1) FROM permission_info
            WHERE enabled=TRUE AND deleted=FALSE AND id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    long countEnabledPermissions(@Param("ids") Collection<Long> ids);

    @Delete("DELETE FROM role_permission_relation WHERE role_id=#{roleId}")
    int deletePermissions(@Param("roleId") Long roleId);

    @Insert("""
            <script>INSERT INTO role_permission_relation(id,role_id,permission_id,created_by,created_at) VALUES
            <foreach collection="items" item="item" separator=",">
            (#{item.id},#{roleId},#{item.targetId},#{operatorId},CURRENT_TIMESTAMP)
            </foreach>
            </script>
            """)
    int insertPermissions(@Param("roleId") Long roleId,
                          @Param("items") Collection<RelationInsertItem> items,
                          @Param("operatorId") Long operatorId);

    @Select("SELECT menu_id FROM role_menu_relation WHERE role_id=#{roleId} ORDER BY menu_id")
    List<Long> selectMenuIds(@Param("roleId") Long roleId);

    @Select("""
            <script>SELECT COUNT(1) FROM menu_info WHERE enabled=TRUE AND deleted=FALSE AND id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    long countEnabledMenus(@Param("ids") Collection<Long> ids);

    @Delete("DELETE FROM role_menu_relation WHERE role_id=#{roleId}")
    int deleteMenus(@Param("roleId") Long roleId);

    @Insert("""
            <script>INSERT INTO role_menu_relation(id,role_id,menu_id,created_by,created_at) VALUES
            <foreach collection="items" item="item" separator=",">
            (#{item.id},#{roleId},#{item.targetId},#{operatorId},CURRENT_TIMESTAMP)
            </foreach>
            </script>
            """)
    int insertMenus(@Param("roleId") Long roleId,
                    @Param("items") Collection<RelationInsertItem> items,
                    @Param("operatorId") Long operatorId);

    @Select("SELECT COUNT(1) FROM user_role_relation WHERE role_id=#{roleId}")
    long countUsers(@Param("roleId") Long roleId);

    @Update("""
            UPDATE user_info u SET auth_version=auth_version+1, updated_at=CURRENT_TIMESTAMP
            WHERE u.deleted=FALSE AND EXISTS (
              SELECT 1 FROM user_role_relation ur
              WHERE ur.role_id=#{roleId} AND ur.user_id=u.id)
            """)
    int incrementUserAuthVersion(@Param("roleId") Long roleId);
}

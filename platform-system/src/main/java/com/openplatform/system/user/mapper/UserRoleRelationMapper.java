package com.openplatform.system.user.mapper;
import com.openplatform.common.database.id.RelationInsertItem;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.*;
/** 用户角色关系数据访问。 */
@Mapper public interface UserRoleRelationMapper {
    @Select("SELECT role_id FROM user_role_relation WHERE user_id=#{userId} ORDER BY role_id")
    List<Long> selectRoleIds(@Param("userId") Long userId);
    @Select("""
      <script>SELECT COUNT(1) FROM role_info WHERE enabled=TRUE AND deleted=FALSE AND id IN
      <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach></script>
      """)
    long countEnabledRoles(@Param("ids") Collection<Long> ids);
    @Delete("DELETE FROM user_role_relation WHERE user_id=#{userId}")
    int deleteRoles(@Param("userId") Long userId);
    @Insert("""
      <script>INSERT INTO user_role_relation(id,user_id,role_id,created_by,created_at) VALUES
      <foreach collection="items" item="item" separator=",">
      (#{item.id},#{userId},#{item.targetId},#{operatorId},CURRENT_TIMESTAMP)
      </foreach></script>
      """)
    int insertRoles(@Param("userId") Long userId,@Param("items") Collection<RelationInsertItem> items,@Param("operatorId") Long operatorId);
    @Update("UPDATE user_info SET auth_version=auth_version+1,updated_by=#{operatorId},updated_at=CURRENT_TIMESTAMP WHERE id=#{userId} AND deleted=FALSE")
    int incrementAuthVersion(@Param("userId") Long userId,@Param("operatorId") Long operatorId);
}

package com.openplatform.system.user.mapper;

import com.openplatform.common.database.id.RelationInsertItem;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 用户岗位关系数据访问。 */
@Mapper
public interface UserPositionRelationMapper {

    @Select("SELECT position_id FROM user_position_relation "
            + "WHERE user_id=#{userId} ORDER BY position_id")
    List<Long> selectPositionIds(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT COUNT(1) FROM position_info
            WHERE enabled=TRUE AND deleted=FALSE AND id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    long countEnabledPositions(@Param("ids") Collection<Long> ids);

    @Delete("DELETE FROM user_position_relation WHERE user_id=#{userId}")
    int deletePositions(@Param("userId") Long userId);

    @Insert("""
            <script>
            INSERT INTO user_position_relation(
                id, user_id, position_id, created_by, created_at
            ) VALUES
            <foreach collection="items" item="item" separator=",">
                (#{item.id}, #{userId}, #{item.targetId}, #{operatorId}, CURRENT_TIMESTAMP)
            </foreach>
            </script>
            """)
    int insertPositions(
            @Param("userId") Long userId,
            @Param("items") Collection<RelationInsertItem> items,
            @Param("operatorId") Long operatorId);
}

package com.openplatform.system.position.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
/** 岗位关联查询。 */
@Mapper public interface PositionReferenceMapper {
    @Select("SELECT COUNT(1) FROM user_position_relation WHERE position_id=#{positionId}")
    long countUsers(@Param("positionId") Long positionId);
}

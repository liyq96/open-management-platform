package com.openplatform.system.menu.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
/** 菜单关联查询。 */
@Mapper public interface MenuReferenceMapper {
    @Select("SELECT COUNT(1) FROM role_menu_relation WHERE menu_id=#{menuId}")
    long countRoleReferences(@Param("menuId") Long menuId);
}

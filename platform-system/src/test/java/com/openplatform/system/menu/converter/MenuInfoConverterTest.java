package com.openplatform.system.menu.converter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.openplatform.system.menu.model.entity.MenuInfo;
import java.util.List;
import org.junit.jupiter.api.Test;
class MenuInfoConverterTest {
    @Test void shouldBuildMenuTree(){MenuInfo child=menu(2L,1L,"用户管理");child.setComponentCode("UserPage");MenuInfo root=menu(1L,null,"系统管理");var tree=MenuInfoConverter.toTree(List.of(child,root));assertEquals(1L,tree.getFirst().getMenuId());assertEquals(2L,tree.getFirst().getChildren().getFirst().getMenuId());assertEquals("UserPage",tree.getFirst().getChildren().getFirst().getComponentCode());}
    private MenuInfo menu(Long id,Long parent,String name){MenuInfo e=new MenuInfo();e.setId(id);e.setParentId(parent);e.setMenuName(name);return e;}
}

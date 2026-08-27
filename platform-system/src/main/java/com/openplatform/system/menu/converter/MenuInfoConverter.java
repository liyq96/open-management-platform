package com.openplatform.system.menu.converter;
import com.openplatform.system.menu.model.entity.MenuInfo;
import com.openplatform.system.menu.model.vo.MenuInfoVO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/** 菜单对象转换器。 */
public final class MenuInfoConverter {
    private MenuInfoConverter(){ }
    public static MenuInfoVO toVO(MenuInfo e){
        MenuInfoVO v=new MenuInfoVO(); v.setMenuId(e.getId());v.setParentId(e.getParentId());v.setMenuName(e.getMenuName());
        v.setRoutePath(e.getRoutePath());v.setComponentCode(e.getComponentCode());v.setIcon(e.getIcon());
        v.setSortOrder(e.getSortOrder());v.setEnabled(e.getEnabled());v.setCreatedAt(e.getCreatedAt());return v;
    }
    public static List<MenuInfoVO> toTree(List<MenuInfo> entities){
        Map<Long,MenuInfoVO> nodes=new LinkedHashMap<>();entities.forEach(e->nodes.put(e.getId(),toVO(e)));
        List<MenuInfoVO> roots=new ArrayList<>();for(MenuInfoVO n:nodes.values()){
            MenuInfoVO p=n.getParentId()==null?null:nodes.get(n.getParentId());if(p==null)roots.add(n);else p.getChildren().add(n);
        } return roots;
    }
}

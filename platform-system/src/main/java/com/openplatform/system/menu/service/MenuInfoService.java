package com.openplatform.system.menu.service;

import com.openplatform.system.menu.model.dto.MenuCreateDTO;
import com.openplatform.system.menu.model.dto.MenuDeleteDTO;
import com.openplatform.system.menu.model.dto.MenuTreeQueryDTO;
import com.openplatform.system.menu.model.dto.MenuUpdateDTO;
import com.openplatform.system.menu.model.vo.MenuInfoVO;
import java.util.List;

/** 菜单管理服务。 */
public interface MenuInfoService {

    List<MenuInfoVO> tree(MenuTreeQueryDTO dto);

    List<MenuInfoVO> currentUserTree();

    MenuInfoVO detail(Long menuId);

    Long create(MenuCreateDTO dto);

    void update(MenuUpdateDTO dto);

    void delete(MenuDeleteDTO dto);
}

package com.openplatform.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.menu.converter.MenuInfoConverter;
import com.openplatform.system.menu.mapper.MenuInfoMapper;
import com.openplatform.system.menu.mapper.MenuReferenceMapper;
import com.openplatform.system.menu.model.dto.MenuCreateDTO;
import com.openplatform.system.menu.model.dto.MenuDeleteDTO;
import com.openplatform.system.menu.model.dto.MenuTreeQueryDTO;
import com.openplatform.system.menu.model.dto.MenuUpdateDTO;
import com.openplatform.system.menu.model.entity.MenuInfo;
import com.openplatform.system.menu.model.vo.MenuInfoVO;
import com.openplatform.system.menu.service.MenuInfoService;
import com.openplatform.system.security.mapper.UserAccessMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 菜单管理服务实现。 */
@Service
@RequiredArgsConstructor
public class MenuInfoServiceImpl implements MenuInfoService {

    private final MenuInfoMapper menuInfoMapper;
    private final MenuReferenceMapper menuReferenceMapper;
    private final UserAccessMapper userAccessMapper;

    @Override
    public List<MenuInfoVO> tree(MenuTreeQueryDTO dto) {
        List<MenuInfo> menus = menuInfoMapper.selectList(new LambdaQueryWrapper<MenuInfo>()
                .eq(dto.getEnabled() != null, MenuInfo::getEnabled, dto.getEnabled())
                .orderByAsc(MenuInfo::getSortOrder)
                .orderByAsc(MenuInfo::getId));
        return MenuInfoConverter.toTree(menus);
    }

    @Override
    public List<MenuInfoVO> currentUserTree() {
        List<MenuInfo> menus = userAccessMapper.selectMenus(SecurityContextUtils.requireUserId());
        return MenuInfoConverter.toTree(menus);
    }

    @Override
    public MenuInfoVO detail(Long menuId) {
        return MenuInfoConverter.toVO(requireMenu(menuId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MenuCreateDTO dto) {
        validateParent(dto.getParentId());
        validateRouteUnique(dto.getRoutePath(), null);
        Long operatorId = SecurityContextUtils.requireUserId();
        MenuInfo entity = new MenuInfo();
        entity.setParentId(dto.getParentId());
        apply(entity, dto.getMenuName(), dto.getRoutePath(), dto.getComponentCode(),
                dto.getIcon(), dto.getSortOrder(), dto.getEnabled());
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setVersion(0);
        entity.setDeleted(false);
        menuInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuUpdateDTO dto) {
        MenuInfo entity = requireMenu(dto.getMenuId());
        if (dto.getMenuId().equals(dto.getParentId())) {
            throw new BusinessException(SystemErrorCode.MENU_PARENT_INVALID);
        }
        validateParent(dto.getParentId());
        validateNoCycle(dto.getMenuId(), dto.getParentId());
        validateRouteUnique(dto.getRoutePath(), dto.getMenuId());
        entity.setParentId(dto.getParentId());
        apply(entity, dto.getMenuName(), dto.getRoutePath(), dto.getComponentCode(),
                dto.getIcon(), dto.getSortOrder(), dto.getEnabled());
        entity.setUpdatedBy(SecurityContextUtils.requireUserId());
        menuInfoMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(MenuDeleteDTO dto) {
        requireMenu(dto.getMenuId());
        Long childCount = menuInfoMapper.selectCount(new LambdaQueryWrapper<MenuInfo>()
                .eq(MenuInfo::getParentId, dto.getMenuId()));
        if (childCount > 0) {
            throw new BusinessException(SystemErrorCode.MENU_HAS_CHILDREN);
        }
        if (menuReferenceMapper.countRoleReferences(dto.getMenuId()) > 0) {
            throw new BusinessException(SystemErrorCode.MENU_IN_USE);
        }
        menuInfoMapper.deleteById(dto.getMenuId());
    }

    private void apply(MenuInfo entity, String name, String route, String componentCode,
            String icon, Integer sortOrder, Boolean enabled) {
        entity.setMenuName(name);
        entity.setRoutePath(route);
        entity.setComponentCode(componentCode);
        entity.setIcon(icon);
        entity.setSortOrder(sortOrder);
        entity.setEnabled(enabled);
    }

    private void validateParent(Long parentId) {
        if (parentId != null) {
            requireMenu(parentId);
        }
    }

    private void validateRouteUnique(String routePath, Long excludedId) {
        if (routePath == null) {
            return;
        }
        Long count = menuInfoMapper.selectCount(new LambdaQueryWrapper<MenuInfo>()
                .eq(MenuInfo::getRoutePath, routePath)
                .ne(excludedId != null, MenuInfo::getId, excludedId));
        if (count > 0) {
            throw new BusinessException(SystemErrorCode.MENU_ROUTE_EXISTS);
        }
    }

    private void validateNoCycle(Long menuId, Long parentId) {
        Set<Long> visitedIds = new HashSet<>();
        Long currentId = parentId;
        while (currentId != null) {
            if (menuId.equals(currentId) || !visitedIds.add(currentId)) {
                throw new BusinessException(SystemErrorCode.MENU_PARENT_INVALID);
            }
            currentId = requireMenu(currentId).getParentId();
        }
    }

    private MenuInfo requireMenu(Long menuId) {
        MenuInfo menu = menuInfoMapper.selectById(menuId);
        if (menu == null) {
            throw new BusinessException(SystemErrorCode.MENU_NOT_FOUND);
        }
        return menu;
    }
}

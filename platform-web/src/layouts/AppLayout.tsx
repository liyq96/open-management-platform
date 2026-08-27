import { useEffect, useMemo, useState } from 'react';
import {
  BellOutlined, LockOutlined, LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined,
} from '@ant-design/icons';
import { Avatar, Button, Drawer, Dropdown, Grid, Layout, Menu, Tooltip, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { getRegisteredMenuComponent, isRegisteredMenuComponent } from '@/app/routeRegistry';
import { getMenuIcon } from '@/config/menuIcons';
import { idKey } from '@/lib/format';
import { authApi } from '@/services/api';
import { useAuthStore } from '@/store/auth';
import type { MenuInfoVO } from '@/types/api';

const isNavigableMenu = (menu: MenuInfoVO, platformAdmin: boolean) => {
  const component = getRegisteredMenuComponent(menu.componentCode);
  return Boolean(menu.routePath && component && (!component.platformOnly || platformAdmin));
};
const menuKey = (menu: MenuInfoVO, platformAdmin: boolean) => isNavigableMenu(menu, platformAdmin) ? menu.routePath! : `menu:${idKey(menu.menuId)}`;

function buildMenuItems(menus: MenuInfoVO[], platformAdmin: boolean): NonNullable<MenuProps['items']> {
  return menus.flatMap((menu) => {
    const component = getRegisteredMenuComponent(menu.componentCode);
    if (component?.platformOnly && !platformAdmin) return [];
    const children = buildMenuItems(menu.children || [], platformAdmin);
    const navigable = isNavigableMenu(menu, platformAdmin);
    const componentMissing = Boolean(menu.routePath && menu.componentCode && !isRegisteredMenuComponent(menu.componentCode));
    if (!navigable && children.length === 0 && !componentMissing) return [];
    return [{
      key: menuKey(menu, platformAdmin),
      icon: getMenuIcon(menu.icon),
      label: componentMissing ? `${menu.menuName}（组件未注册）` : menu.menuName,
      disabled: componentMissing,
      children: children.length > 0 ? children : undefined,
    }];
  });
}

function findAncestorKeys(
  menus: MenuInfoVO[],
  path: string,
  platformAdmin: boolean,
  parents: string[] = [],
): string[] | undefined {
  for (const menu of menus) {
    if (menu.routePath === path) return parents;
    const found = findAncestorKeys(
      menu.children || [], path, platformAdmin, [...parents, menuKey(menu, platformAdmin)],
    );
    if (found) return found;
  }
  return undefined;
}

export function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [openKeys, setOpenKeys] = useState<string[]>([]);
  const screens = Grid.useBreakpoint();
  const isMobile = screens.md === false;
  const navigate = useNavigate();
  const location = useLocation();
  const claims = useAuthStore((state) => state.claims);
  const menus = useAuthStore((state) => state.menus);
  const platformAdmin = Boolean(useAuthStore((state) => state.claims?.platform_admin));
  const clear = useAuthStore((state) => state.clear);
  const menuItems = useMemo(() => buildMenuItems(menus, platformAdmin), [menus, platformAdmin]);
  useEffect(() => {
    if (screens.md === false) {
      setMobileOpen(false);
      return;
    }
    if (screens.xl !== undefined) setCollapsed(!screens.xl);
  }, [screens.md, screens.xl]);
  useEffect(() => {
    setOpenKeys(findAncestorKeys(menus, location.pathname, platformAdmin) || []);
  }, [location.pathname, menus, platformAdmin]);
  const logout = async () => {
    try { await authApi.logout(); } finally { clear(); navigate('/login', { replace: true }); }
  };
  const userMenu: MenuProps['items'] = [
    { key: 'account', icon: <LockOutlined />, label: '修改密码', onClick: () => navigate('/account') },
    { type: 'divider' },
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true, onClick: logout },
  ];
  const onMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (String(key).startsWith('/')) navigate(String(key));
    setMobileOpen(false);
  };
  const navigation = (compact: boolean) => <>
    <div className="brand"><div className="brand__mark">O</div>{!compact && <div><strong>Open Platform</strong><span>企业权限管理平台</span></div>}</div>
    <Menu mode="inline" selectedKeys={[location.pathname]} openKeys={compact ? undefined : openKeys} items={menuItems} onOpenChange={(keys) => setOpenKeys(keys.map(String))} onClick={onMenuClick} />
    <div className="sider-footer"><span className="system-dot" />{!compact && <span>系统服务正常</span>}</div>
  </>;
  return (
    <Layout className="app-shell">
      <a className="skip-link" href="#main-content">跳到主要内容</a>
      {!isMobile && <Layout.Sider className="app-sider" theme="light" width={224} collapsedWidth={76} collapsed={collapsed} trigger={null}>
        {navigation(collapsed)}
      </Layout.Sider>}
      <Drawer className="mobile-nav-drawer" title="主导航" placement="left" width={280} open={isMobile && mobileOpen} closable={false} onClose={() => setMobileOpen(false)}>
        <nav className="mobile-navigation" aria-label="主导航">{navigation(false)}</nav>
      </Drawer>
      <Layout>
        <Layout.Header className="app-header">
          <Button
            type="text"
            aria-label={isMobile ? (mobileOpen ? '关闭主导航' : '打开主导航') : (collapsed ? '展开侧边栏' : '收起侧边栏')}
            icon={(isMobile ? mobileOpen : !collapsed) ? <MenuFoldOutlined /> : <MenuUnfoldOutlined />}
            onClick={() => isMobile ? setMobileOpen((open) => !open) : setCollapsed((value) => !value)}
          />
          <div className="app-header__right">
            <Tooltip title="暂时没有新通知"><Button type="text" aria-label="通知" icon={<BellOutlined />} /></Tooltip>
            <span className="header-separator" />
            <Dropdown menu={{ items: userMenu }} trigger={['click']}>
              <button className="user-chip" type="button">
                <Avatar size={34}>{claims?.username?.slice(0, 1).toUpperCase() || 'U'}</Avatar>
                <span><Typography.Text strong>{claims?.username || '用户'}</Typography.Text><small>租户 {String(claims?.tenant_id ?? '—')}</small></span>
              </button>
            </Dropdown>
          </div>
        </Layout.Header>
        <Layout.Content id="main-content" className="app-content" tabIndex={-1}><Outlet /></Layout.Content>
      </Layout>
    </Layout>
  );
}

import { lazy } from 'react';
import type { ComponentType, LazyExoticComponent } from 'react';
import type { MenuInfoVO } from '@/types/api';

export interface PageComponentDefinition {
  label: string;
  requiredPermissions: readonly string[];
  permissionMode?: 'all' | 'any';
  platformOnly?: boolean;
  Component: LazyExoticComponent<ComponentType>;
}

export interface ResolvedMenuRoute extends PageComponentDefinition {
  menuId: MenuInfoVO['menuId'];
  path: string;
  componentCode: string;
}

const componentRegistry = {
  UserPage: { label: '用户管理', requiredPermissions: ['system:user:list'], Component: lazy(() => import('@/pages/system/UserPage').then((module) => ({ default: module.UserPage }))) },
  DepartmentPage: { label: '部门管理', requiredPermissions: ['system:department:list'], Component: lazy(() => import('@/pages/system/DepartmentPage').then((module) => ({ default: module.DepartmentPage }))) },
  RolePage: { label: '角色管理', requiredPermissions: ['system:role:list'], Component: lazy(() => import('@/pages/system/RolePage').then((module) => ({ default: module.RolePage }))) },
  PermissionPage: { label: '权限管理', requiredPermissions: ['system:permission:list'], Component: lazy(() => import('@/pages/system/PermissionPage').then((module) => ({ default: module.PermissionPage }))) },
  MenuPage: { label: '菜单管理', requiredPermissions: ['system:menu:list'], Component: lazy(() => import('@/pages/system/MenuPage').then((module) => ({ default: module.MenuPage }))) },
  PositionPage: { label: '岗位管理', requiredPermissions: ['system:position:list'], Component: lazy(() => import('@/pages/system/CatalogPages').then((module) => ({ default: module.PositionPage }))) },
  TenantPage: { label: '租户管理', requiredPermissions: ['system:tenant:list'], platformOnly: true, Component: lazy(() => import('@/pages/system/CatalogPages').then((module) => ({ default: module.TenantPage }))) },
  AuditPage: { label: '审计日志', requiredPermissions: ['system:audit:login-list', 'system:audit:operation'], permissionMode: 'any' as const, Component: lazy(() => import('@/pages/system/AuditPage').then((module) => ({ default: module.AuditPage }))) },
} satisfies Record<string, PageComponentDefinition>;

export type MenuComponentCode = keyof typeof componentRegistry;

export const menuComponentOptions = Object.entries(componentRegistry).map(([value, definition]) => ({
  value: value as MenuComponentCode,
  label: `${definition.label}（${value}）`,
  platformOnly: 'platformOnly' in definition && Boolean(definition.platformOnly),
}));

export const getRegisteredMenuComponent = (componentCode?: string): PageComponentDefinition | undefined =>
  componentCode ? componentRegistry[componentCode as MenuComponentCode] : undefined;

export const isRegisteredMenuComponent = (componentCode?: string): componentCode is MenuComponentCode =>
  Boolean(getRegisteredMenuComponent(componentCode));

export function resolveMenuRoutes(items: MenuInfoVO[], platformAdmin = false): ResolvedMenuRoute[] {
  return items.flatMap((item) => {
    const definition = getRegisteredMenuComponent(item.componentCode);
    const current = definition && item.routePath && (!definition.platformOnly || platformAdmin)
      ? [{ ...definition, menuId: item.menuId, path: item.routePath, componentCode: item.componentCode! }]
      : [];
    return [...current, ...resolveMenuRoutes(item.children || [], platformAdmin)];
  });
}

export const hasRoutePermission = (
  route: PageComponentDefinition,
  permissions: Set<string>,
  platformAdmin = false,
) => {
  if (route.platformOnly && !platformAdmin) return false;
  const matches = route.requiredPermissions.map((permission) => permissions.has(permission));
  return route.permissionMode === 'any' ? matches.some(Boolean) : matches.every(Boolean);
};

export const AccountPage = lazy(() => import('@/pages/AccountPage').then((module) => ({ default: module.AccountPage })));

import { lazy, Suspense, useMemo } from 'react';
import { Spin } from 'antd';
import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from '@/app/ProtectedRoute';
import { AccountPage, getRegisteredMenuComponent, hasRoutePermission, resolveMenuRoutes } from '@/app/routeRegistry';
import type { ResolvedMenuRoute } from '@/app/routeRegistry';
import { AppLayout } from '@/layouts/AppLayout';
import { AccessDeniedPage } from '@/pages/AccessDeniedPage';
import { useAuthStore } from '@/store/auth';
import type { MenuInfoVO } from '@/types/api';

const LoginPage = lazy(() => import('@/pages/LoginPage').then((module) => ({ default: module.LoginPage })));
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage').then((module) => ({ default: module.NotFoundPage })));

function findFirstPagePath(items: MenuInfoVO[], permissions: Set<string>, platformAdmin: boolean): string | undefined {
  for (const item of items) {
    const component = getRegisteredMenuComponent(item.componentCode);
    if (component && item.routePath && hasRoutePermission(component, permissions, platformAdmin)) return item.routePath;
    const childPath = findFirstPagePath(item.children || [], permissions, platformAdmin);
    if (childPath) return childPath;
  }
  return undefined;
}

function HomeRedirect() {
  const menus = useAuthStore((state) => state.menus);
  const permissions = useAuthStore((state) => state.permissions);
  const platformAdmin = Boolean(useAuthStore((state) => state.claims?.platform_admin));
  const firstPath = findFirstPagePath(menus, permissions, platformAdmin);
  return firstPath
    ? <Navigate to={firstPath} replace />
    : <AccessDeniedPage title="暂无可使用的管理页面" description="当前账号没有同时满足菜单与查看权限的页面。" showHome={false} />;
}

function AuthorizedMenuPage(route: ResolvedMenuRoute) {
  const menuPaths = useAuthStore((state) => state.menuPaths);
  const permissions = useAuthStore((state) => state.permissions);
  const platformAdmin = Boolean(useAuthStore((state) => state.claims?.platform_admin));
  const { path, requiredPermissions, permissionMode, Component } = route;
  if (!menuPaths.has(path)) return <AccessDeniedPage title="未分配此页面菜单" description="当前角色没有该页面的菜单入口。" />;
  if (!hasRoutePermission(route, permissions, platformAdmin)) return <AccessDeniedPage requiredPermissions={requiredPermissions} permissionMode={permissionMode} />;
  return <Component />;
}

export default function App() {
  const menus = useAuthStore((state) => state.menus);
  const platformAdmin = Boolean(useAuthStore((state) => state.claims?.platform_admin));
  const menuRoutes = useMemo(() => resolveMenuRoutes(menus, platformAdmin), [menus, platformAdmin]);
  return (
    <Suspense fallback={<div className="route-loading"><Spin size="large" tip="正在载入页面…" /></div>}><Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<HomeRedirect />} />
          {menuRoutes.map((route) => <Route key={`${String(route.menuId)}:${route.path}`} path={route.path} element={<AuthorizedMenuPage {...route} />} />)}
          <Route path="/account" element={<AccountPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes></Suspense>
  );
}

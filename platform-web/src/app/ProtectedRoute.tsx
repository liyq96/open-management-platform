import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Navigate, Outlet } from 'react-router-dom';
import { Result, Spin } from 'antd';
import { menuApi, permissionApi } from '@/services/api';
import { useAuthStore } from '@/store/auth';

export function ProtectedRoute() {
  const token = useAuthStore((state) => state.token);
  const claims = useAuthStore((state) => state.claims);
  const setPermissions = useAuthStore((state) => state.setPermissions);
  const setMenus = useAuthStore((state) => state.setMenus);
  const clear = useAuthStore((state) => state.clear);
  const expired = Boolean(claims?.exp && claims.exp * 1000 <= Date.now());
  const query = useQuery({
    queryKey: ['current-access', token],
    queryFn: async () => {
      const [permissions, menus] = await Promise.all([permissionApi.current(), menuApi.current()]);
      setPermissions(permissions);
      setMenus(menus);
      return true;
    },
    enabled: Boolean(token) && !expired,
  });

  useEffect(() => {
    if (expired) clear();
  }, [clear, expired]);
  if (!token || expired) return <Navigate to="/login" replace />;
  if (query.isLoading) return <div className="route-loading"><Spin size="large" tip="正在载入权限…" /></div>;
  if (query.isError) return <Result status="warning" title="暂时无法读取权限" subTitle="请刷新页面或重新登录" />;
  return <Outlet />;
}

import type { ReactNode } from 'react';
import { useAuthStore } from '@/store/auth';

export function Permission({ code, children }: { code?: string; children: ReactNode }) {
  const allowed = useAuthStore((state) => state.hasPermission(code));
  return allowed ? children : null;
}

export function usePermission(code?: string) {
  return useAuthStore((state) => state.hasPermission(code));
}

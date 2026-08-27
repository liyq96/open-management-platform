import { create } from 'zustand';
import type { JwtClaims, MenuInfoVO } from '@/types/api';

const TOKEN_KEY = 'omp_access_token';

function decodeClaims(token?: string | null): JwtClaims | null {
  if (!token) return null;
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const normalized = payload.padEnd(Math.ceil(payload.length / 4) * 4, '=');
    const json = decodeURIComponent(escape(atob(normalized)));
    // Keep long numeric ID claims as their original decimal text before JSON parsing.
    const safeJson = json.replace(/"(user_id|tenant_id|department_id)"\s*:\s*(-?\d+)/g, '"$1":"$2"');
    return JSON.parse(safeJson) as JwtClaims;
  } catch {
    return null;
  }
}

function collectMenuPaths(items: MenuInfoVO[]): string[] {
  return items.flatMap((item) => [item.routePath || '', ...collectMenuPaths(item.children || [])]).filter(Boolean);
}

interface AuthState {
  token: string | null;
  claims: JwtClaims | null;
  permissions: Set<string>;
  menus: MenuInfoVO[];
  menuPaths: Set<string>;
  setToken: (token: string) => void;
  setPermissions: (permissions: string[]) => void;
  setMenus: (menus: MenuInfoVO[]) => void;
  clear: () => void;
  hasPermission: (permission?: string) => boolean;
}

const initialToken = localStorage.getItem(TOKEN_KEY);

export const useAuthStore = create<AuthState>((set, get) => ({
  token: initialToken,
  claims: decodeClaims(initialToken),
  permissions: new Set<string>(),
  menus: [],
  menuPaths: new Set<string>(),
  setToken: (token) => {
    localStorage.setItem(TOKEN_KEY, token);
    set({ token, claims: decodeClaims(token) });
  },
  setPermissions: (permissions) => set({ permissions: new Set(permissions) }),
  setMenus: (menus) => set({ menus, menuPaths: new Set(collectMenuPaths(menus)) }),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    set({ token: null, claims: null, permissions: new Set<string>(), menus: [], menuPaths: new Set<string>() });
  },
  hasPermission: (permission) => !permission || get().permissions.has(permission),
}));

import { useEffect, useState } from 'react';
import type { ComponentType, ReactNode } from 'react';
import {
  ApartmentOutlined,
  AppstoreOutlined,
  AuditOutlined,
  BarsOutlined,
  IdcardOutlined,
  KeyOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  SolutionOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';

export interface MenuIconOption {
  code: string;
  name: string;
}

type AntIconComponent = ComponentType<{ className?: string; 'aria-hidden'?: boolean }>;
type AntIconLibrary = Record<string, AntIconComponent>;

/** 常用图标直接打包，兼容初始化数据以及此前使用的简短编码。 */
const builtInIconComponents: Record<string, AntIconComponent> = {
  app: AppstoreOutlined,
  AppstoreOutlined,
  setting: SettingOutlined,
  SettingOutlined,
  user: UserOutlined,
  UserOutlined,
  team: TeamOutlined,
  TeamOutlined,
  department: ApartmentOutlined,
  ApartmentOutlined,
  role: SolutionOutlined,
  SolutionOutlined,
  permission: SafetyCertificateOutlined,
  SafetyCertificateOutlined,
  menu: BarsOutlined,
  BarsOutlined,
  position: IdcardOutlined,
  IdcardOutlined,
  tenant: KeyOutlined,
  KeyOutlined,
  audit: AuditOutlined,
  AuditOutlined,
};

let loadedIconLibrary: AntIconLibrary | undefined;
let iconLibraryPromise: Promise<AntIconLibrary> | undefined;

function loadIconLibrary(): Promise<AntIconLibrary> {
  if (!iconLibraryPromise) {
    iconLibraryPromise = import('@ant-design/icons')
      .then((module) => {
        loadedIconLibrary = module as unknown as AntIconLibrary;
        return loadedIconLibrary;
      });
  }
  return iconLibraryPromise;
}

function readableIconName(code: string): string {
  return code
    .replace(/Outlined$/, '')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2');
}

/** 进入菜单管理时异步读取 Ant Design Icons 的完整线框图标目录。 */
export async function loadMenuIconOptions(): Promise<MenuIconOption[]> {
  const library = await loadIconLibrary();
  return Object.keys(library)
    .filter((name) => name.endsWith('Outlined'))
    .sort((left, right) => left.localeCompare(right))
    .map((code) => ({ code, name: readableIconName(code) }));
}

function MenuIcon({ code }: { code?: string | null }) {
  const builtIn = code ? builtInIconComponents[code] || builtInIconComponents[code.toLowerCase()] : undefined;
  const loaded = code ? loadedIconLibrary?.[code] : undefined;
  const [IconComponent, setIconComponent] = useState<AntIconComponent>(() => builtIn || loaded || AppstoreOutlined);

  useEffect(() => {
    if (!code) {
      setIconComponent(() => AppstoreOutlined);
      return;
    }
    const immediate = builtInIconComponents[code] || builtInIconComponents[code.toLowerCase()] || loadedIconLibrary?.[code];
    if (immediate) {
      setIconComponent(() => immediate);
      return;
    }
    let active = true;
    void loadIconLibrary().then((library) => {
      if (active) setIconComponent(() => library[code] || AppstoreOutlined);
    });
    return () => { active = false; };
  }, [code]);

  return <IconComponent aria-hidden />;
}

export function getMenuIcon(code?: string | null): ReactNode {
  return <MenuIcon code={code} />;
}

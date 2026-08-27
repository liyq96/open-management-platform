import { useMemo, useState } from 'react';
import { CheckOutlined, CloseOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, Empty, Input, Modal, Spin, Tooltip } from 'antd';
import { getMenuIcon } from '@/config/menuIcons';
import type { MenuIconOption } from '@/config/menuIcons';

interface MenuIconPickerProps {
  value?: string;
  onChange?: (value?: string) => void;
  options: MenuIconOption[];
  loading?: boolean;
}

interface IconCategory {
  key: string;
  label: string;
  keywords?: string[];
}

const commonIconCodes = new Set([
  'AppstoreOutlined', 'HomeOutlined', 'SettingOutlined', 'UserOutlined', 'TeamOutlined',
  'ApartmentOutlined', 'SolutionOutlined', 'SafetyCertificateOutlined', 'BarsOutlined',
  'IdcardOutlined', 'KeyOutlined', 'AuditOutlined', 'LockOutlined', 'FolderOutlined',
  'FileOutlined', 'DatabaseOutlined', 'ApiOutlined', 'CodeOutlined', 'CloudOutlined',
  'MonitorOutlined', 'BarChartOutlined', 'ProjectOutlined', 'CalendarOutlined',
  'MessageOutlined', 'MailOutlined', 'BellOutlined', 'ShopOutlined', 'GlobalOutlined',
  'SearchOutlined', 'ToolOutlined',
]);

const categories: IconCategory[] = [
  { key: 'common', label: '常用' },
  { key: 'all', label: '全部' },
  { key: 'organization', label: '用户组织', keywords: ['user', 'team', 'contact', 'solution', 'idcard', 'apartment', 'crown'] },
  { key: 'navigation', label: '导航方向', keywords: ['arrow', 'caret', 'step', 'swap', 'menu', 'bars', 'appstore', 'home', 'compass', 'enter', 'rollback'] },
  { key: 'content', label: '文件编辑', keywords: ['file', 'folder', 'edit', 'form', 'copy', 'save', 'scissor', 'font', 'align', 'paper', 'book'] },
  { key: 'communication', label: '沟通通知', keywords: ['message', 'mail', 'bell', 'phone', 'comment', 'notification', 'wechat', 'qq', 'sound'] },
  { key: 'data', label: '数据图表', keywords: ['database', 'table', 'chart', 'fund', 'area', 'stock', 'dashboard', 'calculator'] },
  { key: 'business', label: '商业办公', keywords: ['shop', 'shopping', 'bank', 'wallet', 'account', 'project', 'calendar', 'schedule', 'printer', 'credit', 'pay'] },
  { key: 'system', label: '系统开发', keywords: ['setting', 'tool', 'api', 'code', 'cloud', 'bug', 'safety', 'lock', 'key', 'monitor', 'desktop', 'wifi', 'control'] },
  { key: 'media', label: '媒体生活', keywords: ['video', 'audio', 'picture', 'camera', 'play', 'pause', 'heart', 'star', 'like', 'environment', 'car', 'coffee'] },
  { key: 'other', label: '其他' },
];

const semanticCategories = categories.filter((item) => item.key !== 'common' && item.key !== 'all' && item.key !== 'other');

function matchesKeywords(option: MenuIconOption, category: IconCategory): boolean {
  const text = `${option.code} ${option.name}`.toLowerCase();
  return category.keywords?.some((keyword) => text.includes(keyword)) ?? false;
}

function belongsToCategory(option: MenuIconOption, category: IconCategory): boolean {
  if (category.key === 'all') return true;
  if (category.key === 'common') return commonIconCodes.has(option.code);
  if (category.key === 'other') return !semanticCategories.some((item) => matchesKeywords(option, item));
  return matchesKeywords(option, category);
}

export function MenuIconPicker({ value, onChange, options, loading = false }: MenuIconPickerProps) {
  const [open, setOpen] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [categoryKey, setCategoryKey] = useState('common');
  const [visibleLimit, setVisibleLimit] = useState(96);

  const categoryCounts = useMemo(() => Object.fromEntries(categories.map((category) => [
    category.key,
    options.filter((option) => belongsToCategory(option, category)).length,
  ])), [options]);

  const visibleOptions = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();
    if (normalizedKeyword) {
      return options.filter((option) => `${option.name} ${option.code}`.toLowerCase().includes(normalizedKeyword));
    }
    const activeCategory = categories.find((item) => item.key === categoryKey) || categories[0];
    return options.filter((option) => belongsToCategory(option, activeCategory));
  }, [categoryKey, keyword, options]);

  const displayedOptions = visibleOptions.slice(0, visibleLimit);

  return <>
    <div className="menu-icon-picker-field">
      <Tooltip title={value ? `更换图标：${value}` : '选择菜单图标'}>
        <Button className="menu-icon-picker-trigger" aria-label={value ? `更换菜单图标，当前为 ${value}` : '选择菜单图标'} onClick={() => setOpen(true)}>{getMenuIcon(value)}</Button>
      </Tooltip>
      {value && <Tooltip title="清除图标"><Button type="text" icon={<CloseOutlined />} aria-label="清除菜单图标" onClick={() => onChange?.(undefined)} /></Tooltip>}
    </div>
    <Modal className="menu-icon-picker-modal" open={open} width={900} footer={null} title={<div><strong>选择菜单图标</strong><span>按分类浏览，或直接搜索图标名称</span></div>} onCancel={() => setOpen(false)} destroyOnHidden>
      <Input allowClear size="large" prefix={<SearchOutlined />} placeholder="搜索，例如 User、Chart、Setting" value={keyword} onChange={(event) => { setKeyword(event.target.value); setVisibleLimit(96); }} />
      <div className="menu-icon-picker-layout">
        <aside className="menu-icon-categories" aria-label="图标分类">
          {categories.map((category) => <button type="button" key={category.key} className={!keyword && categoryKey === category.key ? 'is-active' : ''} onClick={() => { setKeyword(''); setCategoryKey(category.key); setVisibleLimit(96); }}><span>{category.label}</span><small>{categoryCounts[category.key] || 0}</small></button>)}
        </aside>
        <section className="menu-icon-results" aria-live="polite">
          <header><strong>{keyword ? '搜索结果' : categories.find((item) => item.key === categoryKey)?.label}</strong><span>{visibleOptions.length} 个图标</span></header>
          {loading ? <div className="menu-icon-state"><Spin tip="正在加载图标库" /></div> : visibleOptions.length ? <div className="menu-icon-grid">
            {displayedOptions.map((option) => <button type="button" key={option.code} className={value === option.code ? 'is-selected' : ''} title={option.code} aria-label={`选择图标 ${option.name}`} onClick={() => { onChange?.(option.code); setOpen(false); }}><i>{getMenuIcon(option.code)}</i><span>{option.name}</span>{value === option.code && <em><CheckOutlined /></em>}</button>)}
            {visibleOptions.length > displayedOptions.length && <button type="button" className="menu-icon-load-more" onClick={() => setVisibleLimit((current) => current + 96)}>加载更多（剩余 {visibleOptions.length - displayedOptions.length} 个）</button>}
          </div> : <div className="menu-icon-state"><Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="没有匹配的图标" /></div>}
        </section>
      </div>
    </Modal>
  </>;
}

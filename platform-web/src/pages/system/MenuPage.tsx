import { useEffect, useState } from 'react';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { App, Button, Form, Input, InputNumber, Popconfirm, Select, Space, Switch, Table, Tooltip } from 'antd';
import type { TableColumnsType } from 'antd';
import { EntityDrawer } from '@/components/EntityDrawer';
import { MenuIconPicker } from '@/components/MenuIconPicker';
import { getMenuIcon, loadMenuIconOptions } from '@/config/menuIcons';
import type { MenuIconOption } from '@/config/menuIcons';
import { menuComponentOptions } from '@/app/routeRegistry';
import { PageIntro } from '@/components/PageIntro';
import { Permission } from '@/components/Permission';
import { StatusPill } from '@/components/StatusPill';
import { idKey } from '@/lib/format';
import { menuApi } from '@/services/api';
import { useAuthStore } from '@/store/auth';
import type { EntityId, MenuInfoVO } from '@/types/api';

interface MenuForm { parentId?: EntityId; menuName: string; routePath?: string; componentCode?: string; icon?: string; sortOrder: number; enabled: boolean }
function flatten(items: MenuInfoVO[], level = 0): Array<MenuInfoVO & { level: number }> { return items.flatMap((item) => [{ ...item, level }, ...flatten(item.children || [], level + 1)]); }

export function MenuPage() {
  const platformAdmin = Boolean(useAuthStore((state) => state.claims?.platform_admin));
  const [open, setOpen] = useState(false); const [editing, setEditing] = useState<MenuInfoVO | null>(null); const [form] = Form.useForm<MenuForm>();
  const [iconOptions, setIconOptions] = useState<MenuIconOption[]>([]); const [iconsLoading, setIconsLoading] = useState(true);
  const { message } = App.useApp(); const client = useQueryClient();
  const query = useQuery({ queryKey: ['menus', 'manage'], queryFn: () => menuApi.tree(undefined) }); const options = flatten(query.data || []);
  useEffect(() => { let active = true; void loadMenuIconOptions().then((items) => { if (active) { setIconOptions(items); setIconsLoading(false); } }); return () => { active = false; }; }, []);
  const refresh = () => Promise.all([
    client.invalidateQueries({ queryKey: ['menus'] }),
    client.invalidateQueries({ queryKey: ['current-access'] }),
  ]);
  const save = useMutation({ mutationFn: async (values: MenuForm) => { if (editing) await menuApi.update({ ...values, menuId: editing.menuId }); else await menuApi.create(values); }, onSuccess: async () => { await refresh(); setOpen(false); void message.success(editing ? '菜单已更新' : '菜单已创建'); } });
  const remove = useMutation({ mutationFn: menuApi.delete, onSuccess: async () => { await refresh(); void message.success('菜单已删除'); } });
  const create = (parentId?: EntityId) => { setEditing(null); form.resetFields(); form.setFieldsValue({ parentId, sortOrder: 0, enabled: true }); setOpen(true); };
  const edit = async (row: MenuInfoVO) => { const detail = await menuApi.detail(row.menuId); setEditing(detail); form.setFieldsValue(detail); setOpen(true); };
  const columns: TableColumnsType<MenuInfoVO> = [
    { title: '菜单名称', dataIndex: 'menuName', render: (value) => <strong>{value}</strong> },
    { title: '路由地址', dataIndex: 'routePath', width: 220, render: (value) => <code className="soft-code">{value || '目录'}</code> },
    { title: '组件编码', dataIndex: 'componentCode', width: 230, render: (value) => value || '—' },
    { title: '图标', dataIndex: 'icon', width: 72, align: 'center', render: (value) => value ? <Tooltip title={value}><span className="menu-icon-cell">{getMenuIcon(value)}</span></Tooltip> : '—' },
    { title: '排序', dataIndex: 'sortOrder', width: 80 },
    { title: '状态', dataIndex: 'enabled', width: 105, render: (value) => <StatusPill enabled={value} /> },
    { title: '操作', key: 'actions', width: 250, render: (_, row) => <Space>
      <Permission code="system:menu:create"><Button type="link" size="small" icon={<PlusOutlined />} onClick={() => create(row.menuId)}>新增下级</Button></Permission>
      <Permission code="system:menu:update"><Button type="link" size="small" icon={<EditOutlined />} onClick={() => edit(row)}>编辑</Button></Permission>
      <Permission code="system:menu:delete"><Popconfirm title="删除这个菜单？" onConfirm={() => remove.mutate(row.menuId)}><Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button></Popconfirm></Permission>
    </Space> },
  ];
  return <div className="page-shell">
    <PageIntro title="菜单管理" description="维护后台导航层级与前端路由映射" extra={<Permission code="system:menu:create"><Button type="primary" icon={<PlusOutlined />} onClick={() => create()}>新建菜单</Button></Permission>} />
    <section className="plain-table"><Table rowKey={(row) => idKey(row.menuId)} loading={query.isLoading} dataSource={query.data} columns={columns} pagination={false} scroll={{ x: 1120 }} expandable={{ defaultExpandAllRows: true }} /></section>
    <EntityDrawer open={open} title={editing ? '编辑菜单' : '新建菜单'} description="路由由后端配置，页面组件从前端安全注册表中选择" loading={save.isPending} onClose={() => setOpen(false)} onSubmit={() => form.submit()}>
      <Form form={form} layout="vertical" requiredMark={false} onFinish={(values) => save.mutate(values)}>
        <Form.Item name="parentId" label="上级菜单"><select className="native-select"><option value="">作为一级菜单</option>{options.filter((item) => !editing || idKey(item.menuId) !== idKey(editing.menuId)).map((item) => <option key={idKey(item.menuId)} value={idKey(item.menuId)}>{'　'.repeat(item.level)}{item.menuName}</option>)}</select></Form.Item>
        <Form.Item name="menuName" label="菜单名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <div className="form-grid"><Form.Item name="routePath" label="路由地址"><Input placeholder="/system/user" /></Form.Item><Form.Item name="icon" label="菜单图标" extra={iconsLoading ? '正在加载 Ant Design Icons…' : `共 ${iconOptions.length} 个线框图标`}><MenuIconPicker options={iconOptions} loading={iconsLoading} /></Form.Item></div>
        <Form.Item name="componentCode" label="组件编码" extra="目录可留空；页面只能选择前端已注册组件"><Select allowClear showSearch optionFilterProp="label" placeholder="请选择页面组件" options={menuComponentOptions.filter((option) => platformAdmin || !option.platformOnly)} /></Form.Item>
        <Form.Item name="sortOrder" label="显示顺序"><InputNumber min={0} precision={0} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="enabled" label="菜单状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="停用" /></Form.Item>
      </Form>
    </EntityDrawer>
  </div>;
}

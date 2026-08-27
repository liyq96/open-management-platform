import { useState } from 'react';
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { App, Button, Form, Input, InputNumber, Popconfirm, Select, Space, Spin, Switch, Table, Tree, Typography } from 'antd';
import type { TableColumnsType, TreeDataNode } from 'antd';
import { EntityDrawer } from '@/components/EntityDrawer';
import { FilterBar } from '@/components/FilterBar';
import { PageIntro } from '@/components/PageIntro';
import { Permission } from '@/components/Permission';
import { StatusPill } from '@/components/StatusPill';
import { formatDateTime, idKey } from '@/lib/format';
import { positionApi, tenantApi } from '@/services/api';
import type { EntityId, MenuInfoVO, PositionInfoVO, TenantInfoVO } from '@/types/api';

type CatalogKind = 'position' | 'tenant';
type CatalogRow = PositionInfoVO | TenantInfoVO;

function menuTree(items: MenuInfoVO[]): TreeDataNode[] {
  return items.map((item) => ({
    key: idKey(item.menuId),
    title: item.menuName,
    children: menuTree(item.children || []),
  }));
}

function menuKeys(items: MenuInfoVO[]): React.Key[] {
  return items.flatMap((item) => [idKey(item.menuId), ...menuKeys(item.children || [])]);
}

const definitions = {
  position: { title: '岗位管理', description: '维护组织中的岗位字典，供用户岗位分配使用', id: 'positionId', name: 'positionName', code: 'positionCode', permission: 'system:position', deletable: true },
  tenant: { title: '租户管理', description: '维护平台租户及其启用状态', id: 'tenantId', name: 'tenantName', code: 'tenantCode', permission: 'system:tenant', deletable: false },
} as const;

function CatalogPage({ kind }: { kind: CatalogKind }) {
  const config = definitions[kind]; const [keyword, setKeyword] = useState(''); const [enabled, setEnabled] = useState<boolean | undefined>();
  const [page, setPage] = useState(1); const [pageSize, setPageSize] = useState(20);
  const [open, setOpen] = useState(false); const [editing, setEditing] = useState<CatalogRow | null>(null); const [form] = Form.useForm<Record<string, unknown>>();
  const { message } = App.useApp(); const client = useQueryClient();
  const api = kind === 'position' ? positionApi : tenantApi;
  const query = useQuery({ queryKey: [kind, 'page', keyword, enabled, page, pageSize], queryFn: () => api.page({ keyword: keyword || undefined, enabled, page, pageSize } as never) as Promise<{ records: CatalogRow[]; total: number; page: number; pageSize: number }> });
  const tenantMenus = useQuery({ queryKey: ['tenant', 'menu-options'], queryFn: tenantApi.menuOptions, enabled: kind === 'tenant' && open && !editing });
  const refresh = () => client.invalidateQueries({ queryKey: [kind] });
  const save = useMutation({ mutationFn: async (values: Record<string, unknown>) => { if (editing) await api.update({ ...values, [config.id]: editing[config.id as keyof CatalogRow] } as never); else await api.create(values as never); }, onSuccess: async () => { await refresh(); setOpen(false); void message.success(editing ? '信息已更新' : kind === 'tenant' ? '租户和管理员账号已创建' : '记录已创建'); } });
  const remove = useMutation({ mutationFn: (id: EntityId) => positionApi.delete([id]), onSuccess: async () => { await refresh(); void message.success('记录已删除'); } });
  const create = () => { setEditing(null); form.resetFields(); form.setFieldsValue({ enabled: true, sortOrder: 0, ...(kind === 'tenant' ? { adminUsername: 'admin', adminDisplayName: '租户管理员', menuIds: [] } : {}) }); setOpen(true); };
  const edit = async (row: CatalogRow) => { const id = row[config.id as keyof CatalogRow] as EntityId; const detail = await api.detail(id as never) as CatalogRow; setEditing(detail); form.setFieldsValue(detail as unknown as Record<string, unknown>); setOpen(true); };
  const columns: TableColumnsType<CatalogRow> = [
    { title: kind === 'tenant' ? '租户名称' : '岗位名称', dataIndex: config.name, render: (value) => <strong>{value}</strong> },
    { title: '编码', dataIndex: config.code, width: 260, render: (value) => <code className="soft-code">{value}</code> },
    ...(kind === 'position' ? [{ title: '排序', dataIndex: 'sortOrder', width: 90 }] : []),
    { title: '状态', dataIndex: 'enabled', width: 110, render: (value) => <StatusPill enabled={value} /> },
    { title: '创建时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
    { title: '操作', key: 'actions', fixed: 'right', width: 150, render: (_, row) => <Space>
      <Permission code={`${config.permission}:update`}><Button type="link" size="small" icon={<EditOutlined />} onClick={() => edit(row)}>编辑</Button></Permission>
      {config.deletable && <Permission code={`${config.permission}:delete`}><Popconfirm title="删除这条记录？" onConfirm={() => remove.mutate(row[config.id as keyof CatalogRow] as EntityId)}><Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button></Popconfirm></Permission>}
    </Space> },
  ];
  return <div className="page-shell">
    <PageIntro title={config.title} description={config.description} extra={<Permission code={`${config.permission}:create`}><Button type="primary" icon={<PlusOutlined />} onClick={create}>新建{kind === 'tenant' ? '租户' : kind === 'position' ? '岗位' : '权限'}</Button></Permission>} />
    <section className="plain-table">
      <FilterBar actions={<Typography.Text type="secondary">共 {query.data?.total ?? 0} 条</Typography.Text>}>
        <Input allowClear prefix={<SearchOutlined />} placeholder="搜索名称或编码" value={keyword} onChange={(e) => { setKeyword(e.target.value); setPage(1); }} />
        <Select allowClear placeholder="全部状态" value={enabled} onChange={(value) => { setEnabled(value); setPage(1); }} options={[{ value: true, label: '已启用' }, { value: false, label: '已停用' }]} />
      </FilterBar>
      <Table rowKey={(row) => idKey(row[config.id as keyof CatalogRow] as EntityId)} loading={query.isLoading} columns={columns} dataSource={query.data?.records} scroll={{ x: 900 }} pagination={{ current: page, pageSize, total: query.data?.total, showSizeChanger: true, showTotal: (total) => `共 ${total} 条`, onChange: (next, size) => { setPage(next); setPageSize(size); } }} />
    </section>
    <EntityDrawer open={open} title={editing ? `编辑${config.title.slice(0, 2)}` : `新建${config.title.slice(0, 2)}`} description={kind === 'tenant' && !editing ? '创建租户时同步初始化管理员账号、所选菜单和权限' : '名称和编码必填，最多 25 个字符'} loading={save.isPending} onClose={() => setOpen(false)} onSubmit={() => form.submit()} width={kind === 'tenant' && !editing ? 620 : 520}>
      <Form form={form} layout="vertical" requiredMark={false} onFinish={(values) => save.mutate(values)}>
        <Form.Item name={config.name} label="名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name={config.code} label="编码" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        {kind === 'tenant' && !editing && <>
          <Form.Item name="adminUsername" label="管理员账号" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount autoComplete="off" /></Form.Item>
          <Form.Item name="adminDisplayName" label="管理员名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
          <Form.Item name="adminPassword" label="初始密码" rules={[{ required: true, min: 8, max: 32 }]}><Input.Password autoComplete="new-password" /></Form.Item>
          <Form.Item name="adminConfirmPassword" label="确认初始密码" dependencies={['adminPassword']} rules={[{ required: true, min: 8, max: 32 }, ({ getFieldValue }) => ({ validator: (_, value) => !value || getFieldValue('adminPassword') === value ? Promise.resolve() : Promise.reject(new Error('两次输入的密码不一致')) })]}><Input.Password autoComplete="new-password" /></Form.Item>
          <Form.Item label={<span>初始化菜单 <Typography.Text type="secondary">用于新租户首次登录</Typography.Text></span>} required>
            <div className="tenant-menu-picker__toolbar">
              <Typography.Text type="secondary">选择后将复制菜单并授权给超级管理员，父级目录会自动补齐</Typography.Text>
              <Space size={4}>
                <Button type="link" onClick={() => form.setFieldValue('menuIds', menuKeys(tenantMenus.data || []))}>全选</Button>
                <Button type="link" onClick={() => form.setFieldValue('menuIds', [])}>清空</Button>
              </Space>
            </div>
            {tenantMenus.isError && <Typography.Text type="danger">菜单加载失败，请关闭窗口后重新打开</Typography.Text>}
            <Spin spinning={tenantMenus.isLoading} tip="正在加载菜单">
              <Form.Item name="menuIds" noStyle valuePropName="checkedKeys" trigger="onCheck" getValueFromEvent={(keys) => Array.isArray(keys) ? keys : keys.checked} rules={[{ required: true, type: 'array', min: 1, message: '请至少选择一个初始化菜单' }]}>
                <Tree key={`tenant-menu-${menuKeys(tenantMenus.data || []).length}`} checkable defaultExpandAll treeData={menuTree(tenantMenus.data || [])} />
              </Form.Item>
            </Spin>
          </Form.Item>
        </>}
        {kind === 'position' && <Form.Item name="sortOrder" label="显示顺序"><InputNumber min={0} precision={0} style={{ width: '100%' }} /></Form.Item>}
        <Form.Item name="enabled" label="状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="停用" /></Form.Item>
      </Form>
    </EntityDrawer>
  </div>;
}

export function PositionPage() { return <CatalogPage kind="position" />; }
export function TenantPage() { return <CatalogPage kind="tenant" />; }

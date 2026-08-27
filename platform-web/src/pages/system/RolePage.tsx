import { useMemo, useState } from 'react';
import { DeleteOutlined, EditOutlined, PlusOutlined, SafetyCertificateOutlined, SearchOutlined, SettingOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { App, Button, Checkbox, Form, Input, Popconfirm, Select, Space, Switch, Table, Tree, Typography } from 'antd';
import type { TableColumnsType, TreeDataNode } from 'antd';
import { EntityDrawer } from '@/components/EntityDrawer';
import { FilterBar } from '@/components/FilterBar';
import { PageIntro } from '@/components/PageIntro';
import { Permission } from '@/components/Permission';
import { StatusPill } from '@/components/StatusPill';
import { formatDateTime, idKey } from '@/lib/format';
import { menuApi, permissionApi, permissionGroupApi, roleApi } from '@/services/api';
import type { EntityId, MenuInfoVO, PermissionGroupInfoVO, RoleInfoVO } from '@/types/api';

function menuTree(items: MenuInfoVO[]): TreeDataNode[] { return items.map((item) => ({ key: idKey(item.menuId), title: item.menuName, children: menuTree(item.children || []) })); }
function flattenPermissionGroups(items: PermissionGroupInfoVO[], depth = 0): Array<PermissionGroupInfoVO & { depth: number }> {
  return items.flatMap((item) => [{ ...item, depth }, ...flattenPermissionGroups(item.children || [], depth + 1)]);
}

export function RolePage() {
  const [keyword, setKeyword] = useState(''); const [enabled, setEnabled] = useState<boolean | undefined>();
  const [page, setPage] = useState(1); const [pageSize, setPageSize] = useState(20); const [editing, setEditing] = useState<RoleInfoVO | null>(null);
  const [drawer, setDrawer] = useState<'editor' | 'permissions' | 'menus' | null>(null);
  const [permissionKeyword, setPermissionKeyword] = useState(''); const [selectedPermissionGroupId, setSelectedPermissionGroupId] = useState<EntityId | undefined>();
  const [assignedPermissionIds, setAssignedPermissionIds] = useState<EntityId[]>([]);
  const [form] = Form.useForm<Record<string, unknown>>();
  const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([]); const { message } = App.useApp(); const client = useQueryClient();
  const query = useQuery({ queryKey: ['roles', 'page', keyword, enabled, page, pageSize], queryFn: () => roleApi.page({ keyword: keyword || undefined, enabled, page, pageSize }) });
  const permissions = useQuery({ queryKey: ['permissions', 'role-options'], queryFn: () => permissionApi.page({ enabled: true, page: 1, pageSize: 200 }), enabled: drawer === 'permissions' });
  const permissionGroups = useQuery({ queryKey: ['permission-groups', 'role-options'], queryFn: () => permissionGroupApi.tree(true), enabled: drawer === 'permissions' });
  const menus = useQuery({ queryKey: ['menus', 'role-options'], queryFn: () => menuApi.tree(true), enabled: drawer === 'menus' });
  const flatPermissionGroups = useMemo(() => flattenPermissionGroups(permissionGroups.data || []), [permissionGroups.data]);
  const permissionSections = useMemo(() => flatPermissionGroups
    .filter((group) => selectedPermissionGroupId === undefined || idKey(group.groupId) === idKey(selectedPermissionGroupId))
    .map((group) => ({
      group,
      permissions: (permissions.data?.records || []).filter((permission) => idKey(permission.groupId) === idKey(group.groupId)
        && (!permissionKeyword || permission.permissionName.toLowerCase().includes(permissionKeyword.toLowerCase()) || permission.permissionCode.toLowerCase().includes(permissionKeyword.toLowerCase()))),
    }))
    .filter((section) => section.permissions.length > 0), [flatPermissionGroups, permissionKeyword, permissions.data, selectedPermissionGroupId]);
  const refresh = () => client.invalidateQueries({ queryKey: ['roles'] });
  const refreshCurrentAccess = () => client.invalidateQueries({ queryKey: ['current-access'] });
  const save = useMutation({ mutationFn: async (values: Record<string, unknown>) => { if (editing) await roleApi.update({ ...values, roleId: editing.roleId }); else await roleApi.create(values); }, onSuccess: async () => { await refresh(); setDrawer(null); void message.success(editing ? '角色已更新' : '角色已创建'); } });
  const remove = useMutation({ mutationFn: (id: EntityId) => roleApi.delete([id]), onSuccess: async () => { await refresh(); void message.success('角色已删除'); } });
  const assignPermissions = useMutation({ mutationFn: (ids: EntityId[]) => roleApi.assignPermissions(editing!.roleId, ids), onSuccess: async () => { await Promise.all([refresh(), refreshCurrentAccess()]); setDrawer(null); void message.success('权限分配已保存'); } });
  const assignMenus = useMutation({ mutationFn: (ids: EntityId[]) => roleApi.assignMenus(editing!.roleId, ids), onSuccess: async () => { await Promise.all([refresh(), refreshCurrentAccess()]); setDrawer(null); void message.success('菜单分配已保存'); } });
  const create = () => { setEditing(null); form.resetFields(); form.setFieldsValue({ enabled: true }); setDrawer('editor'); };
  const edit = async (row: RoleInfoVO) => { const detail = await roleApi.detail(row.roleId); setEditing(detail); form.setFieldsValue({ roleCode: detail.roleCode, roleName: detail.roleName, enabled: detail.enabled }); setDrawer('editor'); };
  const openAssignment = async (row: RoleInfoVO, type: 'permissions' | 'menus') => { const detail = await roleApi.detail(row.roleId); setEditing(detail); if (type === 'permissions') { setAssignedPermissionIds(detail.permissionIds); setPermissionKeyword(''); setSelectedPermissionGroupId(undefined); } else setCheckedKeys(detail.menuIds.map(idKey)); setDrawer(type); };
  const togglePermission = (permissionId: EntityId, checked: boolean) => setAssignedPermissionIds((current) => checked
    ? current.some((id) => idKey(id) === idKey(permissionId)) ? current : [...current, permissionId]
    : current.filter((id) => idKey(id) !== idKey(permissionId)));
  const columns: TableColumnsType<RoleInfoVO> = [
    { title: '角色', dataIndex: 'roleName', render: (_, row) => <div className="stacked-cell"><strong>{row.roleName}</strong><small>{row.roleCode}</small></div> },
    { title: '权限 / 菜单', key: 'counts', width: 140, render: (_, row) => <span>{row.permissionIds?.length ?? 0} / {row.menuIds?.length ?? 0}</span> },
    { title: '状态', dataIndex: 'enabled', width: 110, render: (value) => <StatusPill enabled={value} /> },
    { title: '创建时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
    { title: '操作', key: 'actions', fixed: 'right', width: 300, render: (_, row) => <Space size={2} wrap>
      <Permission code="system:role:update"><Button type="link" size="small" icon={<EditOutlined />} onClick={() => edit(row)}>编辑</Button></Permission>
      <Permission code="system:role:permission"><Button type="link" size="small" icon={<SafetyCertificateOutlined />} onClick={() => openAssignment(row, 'permissions')}>权限</Button></Permission>
      <Permission code="system:role:assign-menu"><Button type="link" size="small" icon={<SettingOutlined />} onClick={() => openAssignment(row, 'menus')}>菜单</Button></Permission>
      <Permission code="system:role:delete"><Popconfirm title="删除这个角色？" onConfirm={() => remove.mutate(row.roleId)}><Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button></Popconfirm></Permission>
    </Space> },
  ];
  return <div className="page-shell">
    <PageIntro title="角色管理" description="组合权限和菜单，再分配给平台用户" extra={<Permission code="system:role:create"><Button type="primary" icon={<PlusOutlined />} onClick={create}>新建角色</Button></Permission>} />
    <section className="plain-table">
      <FilterBar actions={<Typography.Text type="secondary">共 {query.data?.total ?? 0} 个角色</Typography.Text>}>
        <Input allowClear prefix={<SearchOutlined />} placeholder="搜索角色名称或编码" value={keyword} onChange={(e) => { setKeyword(e.target.value); setPage(1); }} />
        <Select allowClear placeholder="全部状态" value={enabled} options={[{ value: true, label: '已启用' }, { value: false, label: '已停用' }]} onChange={(value) => { setEnabled(value); setPage(1); }} />
      </FilterBar>
      <Table rowKey={(row) => idKey(row.roleId)} loading={query.isLoading} dataSource={query.data?.records} columns={columns} scroll={{ x: 900 }} pagination={{ current: page, pageSize, total: query.data?.total, showSizeChanger: true, showTotal: (total) => `共 ${total} 条`, onChange: (next, size) => { setPage(next); setPageSize(size); } }} />
    </section>
    <EntityDrawer open={drawer === 'editor'} title={editing ? '编辑角色' : '新建角色'} description="角色编码用于稳定识别，建议创建后谨慎修改" loading={save.isPending} onClose={() => setDrawer(null)} onSubmit={() => form.submit()}>
      <Form form={form} layout="vertical" requiredMark={false} onFinish={(values) => save.mutate(values)}>
        <Form.Item name="roleName" label="角色名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="roleCode" label="角色编码" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="enabled" label="角色状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="停用" /></Form.Item>
      </Form>
    </EntityDrawer>
    <EntityDrawer open={drawer === 'permissions'} title="分配权限" description={editing ? `按独立权限分组设置 ${editing.roleName} 可执行的操作` : undefined} loading={assignPermissions.isPending} onClose={() => setDrawer(null)} onSubmit={() => assignPermissions.mutate(assignedPermissionIds)} width={760}>
          <div className="permission-assignment">
            <aside className="permission-assignment__index">
              <button type="button" className={selectedPermissionGroupId === undefined ? 'is-active' : ''} onClick={() => setSelectedPermissionGroupId(undefined)}><strong>全部分组</strong><small>{permissions.data?.total ?? 0} 项权限</small></button>
              {flatPermissionGroups.map((group) => <button type="button" key={idKey(group.groupId)} className={selectedPermissionGroupId !== undefined && idKey(selectedPermissionGroupId) === idKey(group.groupId) ? 'is-active' : ''} style={{ paddingLeft: 12 + group.depth * 14 }} onClick={() => setSelectedPermissionGroupId(group.groupId)}><strong>{group.groupName}</strong><small>{group.groupCode}</small></button>)}
            </aside>
            <div className="permission-assignment__content">
              <Input allowClear prefix={<SearchOutlined />} placeholder="搜索权限名称或编码" value={permissionKeyword} onChange={(event) => setPermissionKeyword(event.target.value)} />
              <div className="permission-assignment__sections">
                {permissionSections.map(({ group, permissions: items }) => <section key={idKey(group.groupId)}>
                  <header><div><strong>{group.groupName}</strong><small>{group.groupCode}</small></div><span>{items.length} 项</span></header>
                  {items.map((permission) => <Checkbox key={idKey(permission.permissionId)} checked={assignedPermissionIds.some((id) => idKey(id) === idKey(permission.permissionId))} onChange={(event) => togglePermission(permission.permissionId, event.target.checked)}><span><strong>{permission.permissionName}</strong><small>{permission.permissionCode}</small></span><em>{permission.permissionType === 'API' ? '接口' : '按钮'}</em></Checkbox>)}
                </section>)}
                {!permissionSections.length && <div className="permission-assignment__empty">没有匹配的权限</div>}
              </div>
            </div>
          </div>
    </EntityDrawer>
    <EntityDrawer open={drawer === 'menus'} title="分配菜单" description={editing ? `设置 ${editing.roleName} 可见的导航入口` : undefined} loading={assignMenus.isPending} onClose={() => setDrawer(null)} onSubmit={() => assignMenus.mutate(checkedKeys as EntityId[])}>
      <Tree checkable defaultExpandAll checkedKeys={checkedKeys} treeData={menuTree(menus.data || [])} onCheck={(keys) => setCheckedKeys(Array.isArray(keys) ? keys : keys.checked)} />
    </EntityDrawer>
  </div>;
}

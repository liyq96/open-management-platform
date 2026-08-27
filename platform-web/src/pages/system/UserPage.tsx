import { useMemo, useState } from 'react';
import { DeleteOutlined, EditOutlined, KeyOutlined, PlusOutlined, SearchOutlined, TeamOutlined, UserSwitchOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  App, Avatar, Button, Checkbox, Empty, Form, Input, InputNumber, Popconfirm, Select,
  Space, Switch, Table, Tree, Typography,
} from 'antd';
import type { TableColumnsType, TreeDataNode } from 'antd';
import { EntityDrawer } from '@/components/EntityDrawer';
import { FilterBar } from '@/components/FilterBar';
import { PageIntro } from '@/components/PageIntro';
import { Permission } from '@/components/Permission';
import { StatusPill } from '@/components/StatusPill';
import { compactId, formatDateTime, idKey } from '@/lib/format';
import { departmentApi, positionApi, roleApi, userApi } from '@/services/api';
import { useAuthStore } from '@/store/auth';
import type { DepartmentInfoVO, EntityId, UserInfoVO } from '@/types/api';

interface UserFormValues {
  username?: string; password?: string; displayName: string; departmentId?: EntityId;
  email?: string; phone?: string; enabled: boolean;
}

function toTreeData(nodes: DepartmentInfoVO[]): TreeDataNode[] {
  return nodes.map((item) => ({
    key: idKey(item.departmentId), title: item.departmentName,
    children: item.children ? toTreeData(item.children) : undefined,
  }));
}

function flattenDepartments(nodes: DepartmentInfoVO[], level = 0): Array<DepartmentInfoVO & { level: number }> {
  return nodes.flatMap((item) => [{ ...item, level }, ...flattenDepartments(item.children || [], level + 1)]);
}

export function UserPage() {
  const [keyword, setKeyword] = useState('');
  const [enabled, setEnabled] = useState<boolean | undefined>();
  const [departmentId, setDepartmentId] = useState<EntityId | undefined>();
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [editing, setEditing] = useState<UserInfoVO | null>(null);
  const [drawer, setDrawer] = useState<'editor' | 'roles' | 'positions' | 'password' | null>(null);
  const [form] = Form.useForm<UserFormValues>();
  const [assignForm] = Form.useForm<{ ids: EntityId[] }>();
  const [passwordForm] = Form.useForm<{ newPassword: string; confirmPassword: string }>();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const claims = useAuthStore((state) => state.claims);
  const departmentQuery = useQuery({ queryKey: ['departments', 'all'], queryFn: () => departmentApi.tree(undefined) });
  const departmentFlat = useMemo(() => flattenDepartments(departmentQuery.data || []), [departmentQuery.data]);
  const userQuery = useQuery({
    queryKey: ['users', keyword, departmentId, enabled, page, pageSize],
    queryFn: () => userApi.page({ keyword: keyword || undefined, departmentId, enabled, page, pageSize }),
  });
  const roleOptions = useQuery({ queryKey: ['roles', 'options'], queryFn: () => roleApi.page({ enabled: true, page: 1, pageSize: 200 }), enabled: drawer === 'roles' });
  const positionOptions = useQuery({ queryKey: ['positions', 'options'], queryFn: () => positionApi.page({ enabled: true, page: 1, pageSize: 200 }), enabled: drawer === 'positions' });
  const refresh = () => queryClient.invalidateQueries({ queryKey: ['users'] });
  const save = useMutation({
    mutationFn: async (values: UserFormValues) => {
      if (editing) await userApi.update({ ...values, userId: editing.userId });
      else await userApi.create(values);
    },
    onSuccess: async () => { await refresh(); setDrawer(null); void message.success(editing ? '用户信息已更新' : '用户已创建'); },
  });
  const remove = useMutation({
    mutationFn: (userId: EntityId) => userApi.delete([userId]),
    onSuccess: async () => { await refresh(); void message.success('用户已删除'); },
  });
  const assign = useMutation({
    mutationFn: ({ type, ids }: { type: 'roles' | 'positions'; ids: EntityId[] }) => editing
      ? (type === 'roles' ? userApi.assignRoles(editing.userId, ids) : userApi.assignPositions(editing.userId, ids))
      : Promise.resolve(),
    onSuccess: async () => { await refresh(); setDrawer(null); void message.success('分配结果已保存'); },
  });
  const resetPassword = useMutation({
    mutationFn: (values: { newPassword: string; confirmPassword: string }) => userApi.resetPassword({ userId: editing!.userId, ...values }),
    onSuccess: () => { setDrawer(null); void message.success('密码已重置'); },
  });

  const openCreate = () => { setEditing(null); form.resetFields(); form.setFieldsValue({ enabled: true }); setDrawer('editor'); };
  const openEdit = async (record: UserInfoVO) => {
    const detail = await userApi.detail(record.userId); setEditing(detail);
    form.setFieldsValue({ displayName: detail.displayName, departmentId: detail.departmentId, email: detail.email, phone: detail.phone, enabled: detail.enabled }); setDrawer('editor');
  };
  const openAssign = async (record: UserInfoVO, type: 'roles' | 'positions') => {
    const detail = await userApi.detail(record.userId); setEditing(detail);
    assignForm.setFieldsValue({ ids: type === 'roles' ? detail.roleIds : detail.positionIds }); setDrawer(type);
  };
  const openPassword = (record: UserInfoVO) => { setEditing(record); passwordForm.resetFields(); setDrawer('password'); };
  const columns: TableColumnsType<UserInfoVO> = [
    { title: '用户', dataIndex: 'displayName', render: (_, record) => <div className="identity-cell"><Avatar>{record.displayName?.slice(0, 1) || record.username.slice(0, 1)}</Avatar><span><strong>{record.displayName}</strong><small>@{record.username}</small></span></div> },
    { title: '所属部门', dataIndex: 'departmentId', width: 150, render: (id) => departmentFlat.find((item) => idKey(item.departmentId) === idKey(id))?.departmentName || '—' },
    { title: '联系方式', key: 'contact', width: 200, render: (_, record) => <div className="stacked-cell"><span>{record.phone || '—'}</span><small>{record.email || '未填写邮箱'}</small></div> },
    { title: '状态', dataIndex: 'enabled', width: 100, render: (value) => <StatusPill enabled={value} /> },
    { title: '创建时间', dataIndex: 'createdAt', width: 174, render: formatDateTime },
    { title: '操作', key: 'actions', fixed: 'right', width: 255, render: (_, record) => <Space size={2} wrap>
      <Permission code="system:user:update"><Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>编辑</Button></Permission>
      <Permission code="system:user:assign-role"><Button type="link" size="small" icon={<TeamOutlined />} onClick={() => openAssign(record, 'roles')}>角色</Button></Permission>
      <Permission code="system:user:position"><Button type="link" size="small" icon={<UserSwitchOutlined />} onClick={() => openAssign(record, 'positions')}>岗位</Button></Permission>
      <Permission code="system:user:reset-pwd"><Button type="link" size="small" icon={<KeyOutlined />} onClick={() => openPassword(record)}>重置密码</Button></Permission>
      <Permission code="system:user:delete"><Popconfirm title="删除这个用户？" description="删除后将无法继续登录。" onConfirm={() => remove.mutate(record.userId)}><Button type="link" size="small" danger icon={<DeleteOutlined />} disabled={idKey(record.userId) === idKey(claims?.user_id)}>删除</Button></Popconfirm></Permission>
    </Space> },
  ];
  return (
    <div className="page-shell">
      <PageIntro title="用户管理" description="维护平台账号、组织归属与角色岗位关系" extra={<Permission code="system:user:create"><Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新建用户</Button></Permission>} />
      <div className="context-strip"><span><small>当前租户</small><strong>{String(claims?.tenant_id ?? '—')}</strong></span><i /><span><small>所属部门</small><strong>{String(claims?.department_id ?? '—')}</strong></span><i /><span><small>当前身份</small><strong>{claims?.username}</strong></span></div>
      <section className="split-workspace">
        <aside className="tree-pane">
          <div className="pane-heading"><div><strong>组织架构</strong><span>{departmentFlat.length} 个部门</span></div>{departmentId && <Button type="link" size="small" onClick={() => { setDepartmentId(undefined); setPage(1); }}>清除</Button>}</div>
          {departmentQuery.data?.length ? <Tree blockNode defaultExpandAll selectedKeys={departmentId ? [idKey(departmentId)] : []} treeData={toTreeData(departmentQuery.data)} onSelect={(keys) => { setDepartmentId(keys[0] as string | undefined); setPage(1); }} /> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无部门" />}
        </aside>
        <div className="data-pane">
          <FilterBar actions={<Typography.Text type="secondary">共 {userQuery.data?.total ?? 0} 位用户</Typography.Text>}>
            <Input allowClear prefix={<SearchOutlined />} placeholder="搜索账号、姓名、邮箱或手机" value={keyword} onChange={(event) => { setKeyword(event.target.value); setPage(1); }} />
            <Select allowClear placeholder="全部状态" value={enabled} onChange={(value) => { setEnabled(value); setPage(1); }} options={[{ value: true, label: '已启用' }, { value: false, label: '已停用' }]} />
          </FilterBar>
          <Table rowKey={(record) => idKey(record.userId)} loading={userQuery.isLoading} columns={columns} dataSource={userQuery.data?.records} scroll={{ x: 1180 }} pagination={{ current: page, pageSize, total: userQuery.data?.total, showSizeChanger: true, showTotal: (total) => `共 ${total} 条`, onChange: (next, size) => { setPage(next); setPageSize(size); } }} />
        </div>
      </section>

      <EntityDrawer open={drawer === 'editor'} title={editing ? '编辑用户' : '新建用户'} description={editing ? `用户 ID：${compactId(editing.userId)}` : '创建一个可登录的平台账号'} loading={save.isPending} onClose={() => setDrawer(null)} onSubmit={() => form.submit()}>
        <Form form={form} layout="vertical" requiredMark={false} onFinish={(values) => save.mutate(values)}>
        {!editing && <><Form.Item name="username" label="登录账号" rules={[{ required: true, whitespace: true, max: 25 }]}><Input autoComplete="off" maxLength={25} showCount /></Form.Item><Form.Item name="password" label="初始密码" rules={[{ required: true, min: 8, max: 32 }]}><Input.Password autoComplete="new-password" /></Form.Item></>}
        <Form.Item name="displayName" label="显示名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
          <Form.Item name="departmentId" label="所属部门"><Select allowClear showSearch optionFilterProp="label" placeholder="选择部门" options={departmentFlat.map((item) => ({ value: item.departmentId, label: `${'　'.repeat(item.level)}${item.departmentName}` }))} /></Form.Item>
        <div className="form-grid"><Form.Item name="phone" label="手机号码" rules={[{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号码' }]}><Input inputMode="tel" maxLength={11} /></Form.Item><Form.Item name="email" label="邮箱" rules={[{ type: 'email', message: '请输入正确的邮箱地址' }]}><Input inputMode="email" /></Form.Item></div>
          <Form.Item name="enabled" label="账号状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="停用" /></Form.Item>
        </Form>
      </EntityDrawer>

      <EntityDrawer open={drawer === 'roles' || drawer === 'positions'} title={drawer === 'roles' ? '分配角色' : '分配岗位'} description={editing ? `为 ${editing.displayName} 设置关联项` : undefined} loading={assign.isPending} onClose={() => setDrawer(null)} onSubmit={() => assignForm.submit()}>
        <Form form={assignForm} onFinish={({ ids = [] }) => assign.mutate({ type: drawer === 'roles' ? 'roles' : 'positions', ids })}>
          <Form.Item name="ids">
            <Checkbox.Group className="check-list" options={(drawer === 'roles' ? roleOptions.data?.records : positionOptions.data?.records)?.map((item) => ({ label: 'roleName' in item ? `${item.roleName} · ${item.roleCode}` : `${item.positionName} · ${item.positionCode}`, value: 'roleId' in item ? item.roleId : item.positionId }))} />
          </Form.Item>
        </Form>
      </EntityDrawer>

      <EntityDrawer open={drawer === 'password'} title="重置登录密码" description={editing ? `为 ${editing.displayName} 设置新密码` : undefined} loading={resetPassword.isPending} onClose={() => setDrawer(null)} onSubmit={() => passwordForm.submit()}>
        <div className="drawer-notice"><KeyOutlined /><div><strong>重置后原密码立即失效</strong><span>请通过安全渠道将新密码交给用户。</span></div></div>
        <Form form={passwordForm} layout="vertical" requiredMark={false} onFinish={(values) => resetPassword.mutate(values)}>
          <Form.Item name="newPassword" label="新密码" rules={[{ required: true, min: 8, max: 32, message: '请输入 8–32 位密码' }]}><Input.Password autoComplete="new-password" /></Form.Item>
          <Form.Item name="confirmPassword" label="确认新密码" dependencies={['newPassword']} rules={[{ required: true }, ({ getFieldValue }) => ({ validator(_, value) { return value === getFieldValue('newPassword') ? Promise.resolve() : Promise.reject(new Error('两次输入的密码不一致')); } })]}><Input.Password autoComplete="new-password" /></Form.Item>
        </Form>
      </EntityDrawer>
    </div>
  );
}

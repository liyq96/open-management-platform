import { useMemo, useState } from 'react';
import { AppstoreOutlined, DeleteOutlined, EditOutlined, FolderAddOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { App, Button, Form, Input, InputNumber, Popconfirm, Select, Space, Switch, Table, Tree, Typography } from 'antd';
import type { TableColumnsType, TreeDataNode } from 'antd';
import { EntityDrawer } from '@/components/EntityDrawer';
import { FilterBar } from '@/components/FilterBar';
import { PageIntro } from '@/components/PageIntro';
import { Permission } from '@/components/Permission';
import { StatusPill } from '@/components/StatusPill';
import { formatDateTime, idKey } from '@/lib/format';
import { permissionApi, permissionGroupApi } from '@/services/api';
import type { EntityId, PermissionGroupInfoVO, PermissionInfoVO, PermissionType } from '@/types/api';

interface FlatGroup extends PermissionGroupInfoVO {
  depth: number;
}

const ROOT_GROUP_VALUE = '__root_group__';

function flattenGroups(groups: PermissionGroupInfoVO[], depth = 0): FlatGroup[] {
  return groups.flatMap((group) => [
    { ...group, depth },
    ...flattenGroups(group.children || [], depth + 1),
  ]);
}

function groupTree(groups: PermissionGroupInfoVO[]): TreeDataNode[] {
  return groups.map((group) => ({
    key: idKey(group.groupId),
    title: <span className="group-tree-title"><strong>{group.groupName}</strong><small>{group.groupCode}</small></span>,
    children: groupTree(group.children || []),
  }));
}

export function PermissionPage() {
  const [keyword, setKeyword] = useState('');
  const [enabled, setEnabled] = useState<boolean | undefined>();
  const [type, setType] = useState<PermissionType | undefined>();
  const [selectedGroupId, setSelectedGroupId] = useState<EntityId | undefined>();
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [permissionDrawer, setPermissionDrawer] = useState(false);
  const [groupDrawer, setGroupDrawer] = useState(false);
  const [editingPermission, setEditingPermission] = useState<PermissionInfoVO | null>(null);
  const [editingGroup, setEditingGroup] = useState<PermissionGroupInfoVO | null>(null);
  const [permissionForm] = Form.useForm<Record<string, unknown>>();
  const [groupForm] = Form.useForm<Record<string, unknown>>();
  const { message } = App.useApp();
  const client = useQueryClient();

  const groups = useQuery({
    queryKey: ['permission-groups', 'tree'],
    queryFn: () => permissionGroupApi.tree(),
  });
  const flatGroups = useMemo(() => flattenGroups(groups.data || []), [groups.data]);
  const groupMap = useMemo(() => new Map(flatGroups.map((group) => [idKey(group.groupId), group])), [flatGroups]);
  const selectedGroup = selectedGroupId === undefined ? undefined : groupMap.get(idKey(selectedGroupId));
  const query = useQuery({
    queryKey: ['permissions', 'page', keyword, selectedGroupId, enabled, type, page, pageSize],
    queryFn: () => permissionApi.page({ keyword: keyword || undefined, groupId: selectedGroupId, permissionType: type, enabled, page, pageSize }),
  });

  const refreshPermissions = () => client.invalidateQueries({ queryKey: ['permissions'] });
  const refreshGroups = () => client.invalidateQueries({ queryKey: ['permission-groups'] });
  const savePermission = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      if (editingPermission) await permissionApi.update({ ...values, permissionId: editingPermission.permissionId });
      else await permissionApi.create(values);
    },
    onSuccess: async () => {
      await refreshPermissions();
      setPermissionDrawer(false);
      void message.success(editingPermission ? '权限已更新' : '权限已创建');
    },
  });
  const removePermission = useMutation({
    mutationFn: (permissionId: EntityId) => permissionApi.delete([permissionId]),
    onSuccess: async () => {
      await refreshPermissions();
      void message.success('权限已删除');
    },
  });
  const saveGroup = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const payload = { ...values, parentId: values.parentId === ROOT_GROUP_VALUE ? null : values.parentId };
      if (editingGroup) await permissionGroupApi.update({ ...payload, groupId: editingGroup.groupId });
      else await permissionGroupApi.create(payload);
    },
    onSuccess: async () => {
      await Promise.all([refreshGroups(), refreshPermissions()]);
      setGroupDrawer(false);
      void message.success(editingGroup ? '分组已更新' : '分组已创建');
    },
  });
  const removeGroup = useMutation({
    mutationFn: (groupId: EntityId) => permissionGroupApi.delete(groupId),
    onSuccess: async () => {
      setSelectedGroupId(undefined);
      setPage(1);
      await refreshGroups();
      void message.success('分组已删除');
    },
  });

  const createPermission = () => {
    setEditingPermission(null);
    permissionForm.resetFields();
    permissionForm.setFieldsValue({ groupId: selectedGroupId ?? flatGroups[0]?.groupId, permissionType: 'API', enabled: true });
    setPermissionDrawer(true);
  };
  const editPermission = async (row: PermissionInfoVO) => {
    const detail = await permissionApi.detail(row.permissionId);
    setEditingPermission(detail);
    permissionForm.setFieldsValue(detail as unknown as Record<string, unknown>);
    setPermissionDrawer(true);
  };
  const createGroup = () => {
    setEditingGroup(null);
    groupForm.resetFields();
    groupForm.setFieldsValue({ parentId: ROOT_GROUP_VALUE, sortOrder: 0, enabled: true });
    setGroupDrawer(true);
  };
  const editGroup = async () => {
    if (!selectedGroup) return;
    const detail = await permissionGroupApi.detail(selectedGroup.groupId);
    setEditingGroup(detail);
    groupForm.setFieldsValue({ ...detail, parentId: detail.parentId ?? ROOT_GROUP_VALUE } as unknown as Record<string, unknown>);
    setGroupDrawer(true);
  };
  const selectGroup = (keys: React.Key[]) => {
    const group = keys.length ? groupMap.get(String(keys[0])) : undefined;
    setSelectedGroupId(group?.groupId);
    setPage(1);
  };

  const groupOptions = flatGroups
    .filter((group) => !editingGroup || idKey(group.groupId) !== idKey(editingGroup.groupId))
    .map((group) => ({ value: group.groupId, label: `${'　'.repeat(group.depth)}${group.groupName} · ${group.groupCode}` }));
  const parentGroupOptions = [
    { value: ROOT_GROUP_VALUE, label: '顶级分组（无上级）' },
    ...groupOptions,
  ];
  const permissionColumns: TableColumnsType<PermissionInfoVO> = [
    { title: '权限', dataIndex: 'permissionName', render: (_, row) => <div className="stacked-cell"><strong>{row.permissionName}</strong><small>{row.permissionCode}</small></div> },
    { title: '所属分组', dataIndex: 'groupId', width: 170, render: (value: EntityId) => groupMap.get(idKey(value))?.groupName || '未知分组' },
    { title: '类型', dataIndex: 'permissionType', width: 100, render: (value: PermissionType) => <span className="type-pill">{value === 'API' ? '接口' : '按钮'}</span> },
    { title: '状态', dataIndex: 'enabled', width: 100, render: (value) => <StatusPill enabled={value} /> },
    { title: '创建时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
    { title: '操作', key: 'actions', fixed: 'right', width: 150, render: (_, row) => <Space>
      <Permission code="system:permission:update"><Button type="link" size="small" icon={<EditOutlined />} onClick={() => editPermission(row)}>编辑</Button></Permission>
      <Permission code="system:permission:delete"><Popconfirm title="删除这个权限？" description="已分配给角色的权限不能删除。" onConfirm={() => removePermission.mutate(row.permissionId)}><Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button></Popconfirm></Permission>
    </Space> },
  ];

  return <div className="page-shell">
    <PageIntro
      title="权限管理"
      description="按业务能力组织权限标识；权限分组只负责分类，与菜单结构相互独立"
      extra={<Permission code="system:permission:create"><Button type="primary" icon={<PlusOutlined />} disabled={!flatGroups.length} onClick={createPermission}>新建权限</Button></Permission>}
    />
    <section className="split-workspace permission-workspace">
      <aside className="tree-pane permission-index">
        <div className="pane-heading">
          <div><strong>权限分组</strong><span>{flatGroups.length} 个业务分组</span></div>
        </div>
        <Permission code="system:permission:create"><Button className="group-create-button" type="primary" block icon={<FolderAddOutlined />} onClick={createGroup}>新建分组</Button></Permission>
        <button className={`group-index-all ${selectedGroupId === undefined ? 'is-active' : ''}`} type="button" onClick={() => { setSelectedGroupId(undefined); setPage(1); }}>
          <AppstoreOutlined /><span><strong>全部权限</strong><small>查看所有分组</small></span>
        </button>
        <Tree
          blockNode
          defaultExpandAll
          selectedKeys={selectedGroupId === undefined ? [] : [idKey(selectedGroupId)]}
          treeData={groupTree(groups.data || [])}
          onSelect={selectGroup}
        />
        {selectedGroup && <div className="group-index-actions">
          <Permission code="system:permission:update"><Button type="text" size="small" icon={<EditOutlined />} onClick={editGroup}>编辑分组</Button></Permission>
          <Permission code="system:permission:delete"><Popconfirm title="删除这个分组？" description="请先移除其下级分组和权限。" onConfirm={() => removeGroup.mutate(selectedGroup.groupId)}><Button type="text" size="small" danger icon={<DeleteOutlined />}>删除</Button></Popconfirm></Permission>
        </div>}
      </aside>
      <div className="data-pane">
        <FilterBar actions={<Typography.Text type="secondary">{selectedGroup ? selectedGroup.groupName : '全部分组'} · {query.data?.total ?? 0} 条</Typography.Text>}>
          <Input allowClear prefix={<SearchOutlined />} placeholder="搜索权限名称或编码" value={keyword} onChange={(event) => { setKeyword(event.target.value); setPage(1); }} />
          <Select allowClear placeholder="全部类型" value={type} onChange={(value) => { setType(value); setPage(1); }} options={[{ value: 'API', label: '接口权限' }, { value: 'BUTTON', label: '按钮权限' }]} />
          <Select allowClear placeholder="全部状态" value={enabled} onChange={(value) => { setEnabled(value); setPage(1); }} options={[{ value: true, label: '已启用' }, { value: false, label: '已停用' }]} />
        </FilterBar>
        <Table
          rowKey={(row) => idKey(row.permissionId)}
          loading={query.isLoading}
          columns={permissionColumns}
          dataSource={query.data?.records}
          scroll={{ x: 900 }}
          pagination={{ current: page, pageSize, total: query.data?.total, showSizeChanger: true, showTotal: (total) => `共 ${total} 条`, onChange: (next, size) => { setPage(next); setPageSize(size); } }}
        />
      </div>
    </section>

    <EntityDrawer open={permissionDrawer} title={editingPermission ? '编辑权限' : '新建权限'} description="名称和编码最多 25 个字符" loading={savePermission.isPending} onClose={() => setPermissionDrawer(false)} onSubmit={() => permissionForm.submit()}>
      <Form form={permissionForm} layout="vertical" requiredMark={false} onFinish={(values) => savePermission.mutate(values)}>
        <Form.Item name="groupId" label="所属分组" rules={[{ required: true, message: '请选择权限分组' }]}><Select showSearch optionFilterProp="label" options={groupOptions} /></Form.Item>
        <Form.Item name="permissionName" label="权限名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="permissionCode" label="权限编码" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="permissionType" label="权限类型" rules={[{ required: true }]}><Select options={[{ value: 'API', label: '接口权限' }, { value: 'BUTTON', label: '按钮权限' }]} /></Form.Item>
        <Form.Item name="enabled" label="状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="停用" /></Form.Item>
      </Form>
    </EntityDrawer>

    <EntityDrawer open={groupDrawer} title={editingGroup ? '编辑权限分组' : '新建权限分组'} description="分组用于整理权限，不决定菜单可见性" loading={saveGroup.isPending} onClose={() => setGroupDrawer(false)} onSubmit={() => groupForm.submit()}>
      <Form form={groupForm} layout="vertical" requiredMark={false} onFinish={(values) => saveGroup.mutate(values)}>
        <Form.Item name="parentId" label="上级分组" extra="选择“顶级分组”时，该分组会显示在分组树最外层。"><Select showSearch optionFilterProp="label" options={parentGroupOptions} /></Form.Item>
        <Form.Item name="groupName" label="分组名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="groupCode" label="分组编码" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="sortOrder" label="显示顺序" rules={[{ required: true }]}><InputNumber min={0} precision={0} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="enabled" label="状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="停用" /></Form.Item>
      </Form>
    </EntityDrawer>
  </div>;
}

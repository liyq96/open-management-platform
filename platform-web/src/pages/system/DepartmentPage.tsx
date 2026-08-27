import { useState } from 'react';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { App, Button, Form, Input, InputNumber, Popconfirm, Space, Switch, Table } from 'antd';
import type { TableColumnsType } from 'antd';
import { EntityDrawer } from '@/components/EntityDrawer';
import { PageIntro } from '@/components/PageIntro';
import { Permission } from '@/components/Permission';
import { StatusPill } from '@/components/StatusPill';
import { formatDateTime, idKey } from '@/lib/format';
import { departmentApi } from '@/services/api';
import type { DepartmentInfoVO, EntityId } from '@/types/api';

interface DepartmentForm { parentId?: EntityId; departmentCode: string; departmentName: string; sortOrder: number; enabled: boolean }

function flattenOptions(items: DepartmentInfoVO[], level = 0): Array<DepartmentInfoVO & { level: number }> {
  return items.flatMap((item) => [{ ...item, level }, ...flattenOptions(item.children || [], level + 1)]);
}

export function DepartmentPage() {
  const [open, setOpen] = useState(false); const [editing, setEditing] = useState<DepartmentInfoVO | null>(null);
  const [form] = Form.useForm<DepartmentForm>(); const { message } = App.useApp(); const client = useQueryClient();
  const query = useQuery({ queryKey: ['departments', 'manage'], queryFn: () => departmentApi.tree(undefined) });
  const options = flattenOptions(query.data || []);
  const refresh = () => client.invalidateQueries({ queryKey: ['departments'] });
  const save = useMutation({ mutationFn: async (values: DepartmentForm) => { if (editing) await departmentApi.update({ ...values, departmentId: editing.departmentId }); else await departmentApi.create(values); }, onSuccess: async () => { await refresh(); setOpen(false); void message.success(editing ? '部门已更新' : '部门已创建'); } });
  const remove = useMutation({ mutationFn: departmentApi.delete, onSuccess: async () => { await refresh(); void message.success('部门已删除'); } });
  const showCreate = (parentId?: EntityId) => { setEditing(null); form.resetFields(); form.setFieldsValue({ parentId, sortOrder: 0, enabled: true }); setOpen(true); };
  const showEdit = async (record: DepartmentInfoVO) => { const detail = await departmentApi.detail(record.departmentId); setEditing(detail); form.setFieldsValue(detail); setOpen(true); };
  const columns: TableColumnsType<DepartmentInfoVO> = [
    { title: '部门名称', dataIndex: 'departmentName', render: (name, record) => <div className="tree-name"><span className="tree-name__dot" /><span><strong>{name}</strong><small>{record.departmentCode}</small></span></div> },
    { title: '排序', dataIndex: 'sortOrder', width: 90 },
    { title: '状态', dataIndex: 'enabled', width: 110, render: (value) => <StatusPill enabled={value} /> },
    { title: '创建时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
    { title: '操作', key: 'actions', width: 260, render: (_, record) => <Space>
      <Permission code="system:department:create"><Button type="link" size="small" icon={<PlusOutlined />} onClick={() => showCreate(record.departmentId)}>新增下级</Button></Permission>
      <Permission code="system:department:update"><Button type="link" size="small" icon={<EditOutlined />} onClick={() => showEdit(record)}>编辑</Button></Permission>
      <Permission code="system:department:delete"><Popconfirm title="删除这个部门？" description="存在下级部门或用户时，后端将拒绝删除。" onConfirm={() => remove.mutate(record.departmentId)}><Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button></Popconfirm></Permission>
    </Space> },
  ];
  return <div className="page-shell">
    <PageIntro title="部门管理" description="以树形结构维护组织层级和用户归属" extra={<Permission code="system:department:create"><Button type="primary" icon={<PlusOutlined />} onClick={() => showCreate()}>新建部门</Button></Permission>} />
    <section className="plain-table"><Table rowKey={(row) => idKey(row.departmentId)} loading={query.isLoading} dataSource={query.data} columns={columns} pagination={false} scroll={{ x: 900 }} expandable={{ defaultExpandAllRows: true, childrenColumnName: 'children', indentSize: 26 }} /></section>
    <EntityDrawer open={open} title={editing ? '编辑部门' : '新建部门'} description="维护组织层级、编码与展示顺序" loading={save.isPending} onClose={() => setOpen(false)} onSubmit={() => form.submit()}>
      <Form form={form} layout="vertical" requiredMark={false} onFinish={(values) => save.mutate(values)}>
        <Form.Item name="parentId" label="上级部门"><select className="native-select"><option value="">作为一级部门</option>{options.filter((item) => !editing || idKey(item.departmentId) !== idKey(editing.departmentId)).map((item) => <option key={idKey(item.departmentId)} value={idKey(item.departmentId)}>{'　'.repeat(item.level)}{item.departmentName}</option>)}</select></Form.Item>
        <Form.Item name="departmentName" label="部门名称" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="departmentCode" label="部门编码" rules={[{ required: true, whitespace: true, max: 25 }]}><Input maxLength={25} showCount /></Form.Item>
        <Form.Item name="sortOrder" label="显示顺序" rules={[{ required: true }]}><InputNumber min={0} precision={0} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="enabled" label="部门状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="停用" /></Form.Item>
      </Form>
    </EntityDrawer>
  </div>;
}

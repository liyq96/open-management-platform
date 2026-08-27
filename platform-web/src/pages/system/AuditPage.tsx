import { useState } from 'react';
import { CheckCircleOutlined, CloseCircleOutlined, SearchOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Input, Select, Table, Tabs, Typography } from 'antd';
import type { TableColumnsType, TabsProps } from 'antd';
import { FilterBar } from '@/components/FilterBar';
import { PageIntro } from '@/components/PageIntro';
import { formatDateTime, idKey } from '@/lib/format';
import { auditApi } from '@/services/api';
import { useAuthStore } from '@/store/auth';
import type { LoginLogVO, OperationLogVO } from '@/types/api';

function ResultMark({ success }: { success: boolean }) {
  return <span className={`result-mark ${success ? 'is-success' : 'is-failed'}`}>{success ? <CheckCircleOutlined /> : <CloseCircleOutlined />}{success ? '成功' : '失败'}</span>;
}

function LoginLogs() {
  const [username, setUsername] = useState(''); const [success, setSuccess] = useState<boolean | undefined>(); const [page, setPage] = useState(1); const [pageSize, setPageSize] = useState(20);
  const query = useQuery({ queryKey: ['audit', 'login', username, success, page, pageSize], queryFn: () => auditApi.loginPage({ username: username || undefined, success, page, pageSize }) });
  const columns: TableColumnsType<LoginLogVO> = [
    { title: '账号', dataIndex: 'username', width: 150, render: (value) => <strong>{value}</strong> },
    { title: '登录结果', dataIndex: 'success', width: 110, render: (value) => <ResultMark success={value} /> },
    { title: '登录 IP', dataIndex: 'loginIp', width: 150, render: (value) => value || '—' },
    { title: '失败原因', dataIndex: 'failureReason', width: 220, render: (value) => value || '—' },
    { title: '客户端', dataIndex: 'userAgent', ellipsis: true, render: (value) => value || '—' },
    { title: '时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
  ];
  return <><FilterBar actions={<Typography.Text type="secondary">共 {query.data?.total ?? 0} 条记录</Typography.Text>}><Input allowClear prefix={<SearchOutlined />} placeholder="搜索登录账号" value={username} onChange={(e) => { setUsername(e.target.value); setPage(1); }} /><Select allowClear placeholder="全部结果" value={success} onChange={(value) => { setSuccess(value); setPage(1); }} options={[{ value: true, label: '登录成功' }, { value: false, label: '登录失败' }]} /></FilterBar><Table rowKey={(row) => idKey(row.logId)} loading={query.isLoading} dataSource={query.data?.records} columns={columns} scroll={{ x: 1050 }} pagination={{ current: page, pageSize, total: query.data?.total, showSizeChanger: true, onChange: (next, size) => { setPage(next); setPageSize(size); } }} /></>;
}

function OperationLogs() {
  const [moduleName, setModuleName] = useState(''); const [success, setSuccess] = useState<boolean | undefined>(); const [page, setPage] = useState(1); const [pageSize, setPageSize] = useState(20);
  const query = useQuery({ queryKey: ['audit', 'operation', moduleName, success, page, pageSize], queryFn: () => auditApi.operationPage({ moduleName: moduleName || undefined, success, page, pageSize }) });
  const columns: TableColumnsType<OperationLogVO> = [
    { title: '模块', dataIndex: 'moduleName', width: 140, render: (value) => <span className="type-pill">{value}</span> },
    { title: '操作', dataIndex: 'operationName', width: 140, render: (value) => <strong>{value}</strong> },
    { title: '结果', dataIndex: 'success', width: 100, render: (value) => <ResultMark success={value} /> },
    { title: '用户 ID', dataIndex: 'userId', width: 130, render: (value) => value ? String(value) : '—' },
    { title: '请求路径', dataIndex: 'requestPath', width: 260, render: (value) => <code className="soft-code">{value || '—'}</code> },
    { title: '请求 ID', dataIndex: 'requestId', ellipsis: true, render: (value) => value || '—' },
    { title: '时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
  ];
  return <><FilterBar actions={<Typography.Text type="secondary">共 {query.data?.total ?? 0} 条记录</Typography.Text>}><Input allowClear prefix={<SearchOutlined />} placeholder="搜索模块名称" value={moduleName} onChange={(e) => { setModuleName(e.target.value); setPage(1); }} /><Select allowClear placeholder="全部结果" value={success} onChange={(value) => { setSuccess(value); setPage(1); }} options={[{ value: true, label: '操作成功' }, { value: false, label: '操作失败' }]} /></FilterBar><Table rowKey={(row) => idKey(row.logId)} loading={query.isLoading} dataSource={query.data?.records} columns={columns} scroll={{ x: 1150 }} pagination={{ current: page, pageSize, total: query.data?.total, showSizeChanger: true, onChange: (next, size) => { setPage(next); setPageSize(size); } }} /></>;
}

export function AuditPage() {
  const permissions = useAuthStore((state) => state.permissions);
  const items: NonNullable<TabsProps['items']> = [];
  if (permissions.has('system:audit:login-list')) items.push({ key: 'login', label: '登录日志', children: <LoginLogs /> });
  if (permissions.has('system:audit:operation')) items.push({ key: 'operation', label: '操作日志', children: <OperationLogs /> });
  return <div className="page-shell"><PageIntro title="审计日志" description="追踪登录行为与关键管理操作，便于安全核查" /><section className="plain-table audit-panel"><Tabs items={items} /></section></div>;
}

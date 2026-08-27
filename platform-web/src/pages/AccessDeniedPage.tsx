import { AppstoreOutlined, ArrowLeftOutlined, LockOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Space } from 'antd';
import { useNavigate } from 'react-router-dom';

interface AccessDeniedPageProps {
  title?: string;
  description?: string;
  requiredPermissions?: readonly string[];
  permissionMode?: 'all' | 'any';
  showHome?: boolean;
}

export function AccessDeniedPage({
  title = '暂时无法查看此页面',
  description = '当前账号已经拥有此菜单入口，但缺少页面所需的查看权限。',
  requiredPermissions = [],
  permissionMode = 'all',
  showHome = true,
}: AccessDeniedPageProps) {
  const navigate = useNavigate();
  return <div className="page-shell access-denied-page">
    <section className="access-denied" aria-labelledby="access-denied-title">
      <div className="access-denied__mark" aria-hidden="true">
        <span><LockOutlined /></span>
        <strong>403</strong>
      </div>
      <div className="access-denied__content">
        <span className="access-denied__eyebrow">访问权限未满足</span>
        <h1 id="access-denied-title">{title}</h1>
        <p>{description} 请联系管理员调整角色权限后重试。</p>
        {requiredPermissions.length > 0 && <div className="access-denied__requirement">
          <small>{permissionMode === 'any' && requiredPermissions.length > 1 ? '需要以下任一权限' : '所需权限'}</small>
          <div>{requiredPermissions.map((permission) => <code key={permission}>{permission}</code>)}</div>
        </div>}
        <Space className="access-denied__actions" wrap>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回上一页</Button>
          {showHome
            ? <Button type="primary" icon={<AppstoreOutlined />} onClick={() => navigate('/')}>前往可访问页面</Button>
            : <Button type="primary" icon={<ReloadOutlined />} onClick={() => window.location.reload()}>刷新权限</Button>}
        </Space>
      </div>
    </section>
  </div>;
}

import { Button, Result } from 'antd';
import { useNavigate } from 'react-router-dom';

export function NotFoundPage() {
  const navigate = useNavigate();
  return <Result status="404" title="页面不存在" subTitle="这个地址没有对应的管理功能" extra={<Button type="primary" onClick={() => navigate('/')}>返回可访问页面</Button>} />;
}

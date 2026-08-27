import { useEffect } from 'react';
import { BankOutlined, LockOutlined, ReloadOutlined, SafetyCertificateFilled, UserOutlined } from '@ant-design/icons';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Alert, Button, Form, Input, Typography } from 'antd';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { authApi } from '@/services/api';
import { useAuthStore } from '@/store/auth';
import type { UserLoginDTO } from '@/types/api';

export function LoginPage() {
  const [form] = Form.useForm<UserLoginDTO>();
  const token = useAuthStore((state) => state.token);
  const setToken = useAuthStore((state) => state.setToken);
  const navigate = useNavigate();
  const location = useLocation();
  const captcha = useQuery({ queryKey: ['captcha'], queryFn: authApi.captcha, staleTime: 0, refetchOnWindowFocus: false });
  const login = useMutation({
    mutationFn: authApi.login,
    onSuccess: (result) => {
      setToken(result.token);
      const from = (location.state as { from?: string } | null)?.from || '/system/user';
      navigate(from, { replace: true });
    },
    onError: () => { form.setFieldValue('captchaCode', ''); void captcha.refetch(); },
  });
  useEffect(() => { if (captcha.data) form.setFieldValue('captchaId', captcha.data.captchaId); }, [captcha.data, form]);
  if (token) return <Navigate to="/system/user" replace />;
  return (
    <main className="login-page">
      <section className="login-story">
        <div className="brand brand--login"><div className="brand__mark">O</div><div><strong>Open Platform</strong><span>企业权限管理平台</span></div></div>
        <div className="login-story__copy">
          <span className="eyebrow"><SafetyCertificateFilled /> 权限清晰，管理从容</span>
          <h1>让组织、角色与权限，<br />回到一条清晰的线上。</h1>
          <p>面向企业的统一身份和权限管理入口，所有操作都由后端授权结果约束并留下审计记录。</p>
        </div>
        <div className="login-orbit" aria-hidden="true"><i /><i /><i /><span /></div>
        <small className="login-story__foot">Open Management Platform</small>
      </section>
      <section className="login-panel">
        <div className="login-box">
          <Typography.Title level={2}>欢迎回来</Typography.Title>
          <Typography.Paragraph type="secondary">使用平台账号继续访问管理后台</Typography.Paragraph>
          <Form form={form} layout="vertical" size="large" initialValues={{ tenantCode: 'platform' }} onFinish={(values) => login.mutate(values)} requiredMark={false}>
            <Form.Item name="tenantCode" label="租户编码" rules={[{ required: true, whitespace: true, max: 25, message: '请输入不超过 25 个字符的租户编码' }]}><Input prefix={<BankOutlined />} autoComplete="organization" maxLength={25} placeholder="请输入租户编码" /></Form.Item>
            <Form.Item name="username" label="账号" rules={[{ required: true, whitespace: true, max: 25, message: '请输入不超过 25 个字符的账号' }]}><Input prefix={<UserOutlined />} autoComplete="username" maxLength={25} placeholder="请输入账号" /></Form.Item>
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}><Input.Password prefix={<LockOutlined />} autoComplete="current-password" placeholder="请输入密码" /></Form.Item>
            <Form.Item name="captchaId" hidden><Input /></Form.Item>
            <Form.Item label="验证码" required>
              <div className="captcha-row">
                <Form.Item name="captchaCode" noStyle rules={[{ required: true, message: '请输入验证码' }]}><Input placeholder="输入图中字符" autoComplete="off" /></Form.Item>
                <button type="button" className="captcha-image" aria-label="换一张验证码" onClick={() => captcha.refetch()}>
                  {captcha.data?.imageBase64 ? <img src={captcha.data.imageBase64} alt="验证码" /> : <ReloadOutlined spin={captcha.isFetching} />}
                </button>
              </div>
            </Form.Item>
            {login.isError && <Alert className="login-error" type="error" showIcon message="登录未成功，请检查账号、密码和验证码" />}
            <Button block type="primary" htmlType="submit" loading={login.isPending}>进入管理平台</Button>
          </Form>
          <p className="login-help">遇到账号问题，请联系平台管理员</p>
        </div>
      </section>
    </main>
  );
}

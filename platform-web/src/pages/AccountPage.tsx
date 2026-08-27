import { LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { useMutation } from '@tanstack/react-query';
import { App, Button, Form, Input } from 'antd';
import { useNavigate } from 'react-router-dom';
import { PageIntro } from '@/components/PageIntro';
import { userApi } from '@/services/api';

interface PasswordForm { oldPassword: string; newPassword: string; confirmPassword: string }

export function AccountPage() {
  const [form] = Form.useForm<PasswordForm>();
  const { message } = App.useApp();
  const navigate = useNavigate();
  const mutation = useMutation({
    mutationFn: userApi.changePassword,
    onSuccess: () => { void message.success('密码已修改，请使用新密码妥善保管'); form.resetFields(); },
  });
  return (
    <div className="page-shell narrow-page">
      <PageIntro title="账号安全" description="修改当前登录账号的密码" />
      <section className="account-panel">
        <div className="account-panel__icon"><SafetyCertificateOutlined /></div>
        <div className="account-panel__body">
          <h2>登录密码</h2><p>建议使用 8–32 位、包含字母和数字的独立密码。</p>
          <Form form={form} layout="vertical" onFinish={(values) => mutation.mutate(values)} requiredMark={false}>
            <Form.Item name="oldPassword" label="当前密码" rules={[{ required: true, message: '请输入当前密码' }]}><Input.Password prefix={<LockOutlined />} autoComplete="current-password" /></Form.Item>
            <Form.Item name="newPassword" label="新密码" rules={[{ required: true, min: 8, max: 32, message: '请输入 8–32 位新密码' }]}><Input.Password prefix={<LockOutlined />} autoComplete="new-password" /></Form.Item>
            <Form.Item name="confirmPassword" label="确认新密码" dependencies={['newPassword']} rules={[{ required: true }, ({ getFieldValue }) => ({ validator(_, value) { return !value || getFieldValue('newPassword') === value ? Promise.resolve() : Promise.reject(new Error('两次输入的新密码不一致')); } })]}><Input.Password prefix={<LockOutlined />} autoComplete="new-password" /></Form.Item>
            <div className="form-actions"><Button onClick={() => navigate(-1)}>返回</Button><Button type="primary" htmlType="submit" loading={mutation.isPending}>确认修改</Button></div>
          </Form>
        </div>
      </section>
    </div>
  );
}

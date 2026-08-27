import { CheckCircleFilled, MinusCircleFilled } from '@ant-design/icons';

export function StatusPill({ enabled, trueText = '已启用', falseText = '已停用' }: { enabled: boolean; trueText?: string; falseText?: string }) {
  return <span className={`status-pill ${enabled ? 'is-on' : 'is-off'}`}>{enabled ? <CheckCircleFilled /> : <MinusCircleFilled />}{enabled ? trueText : falseText}</span>;
}

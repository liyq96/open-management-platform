import type { ReactNode } from 'react';
import { Button, Drawer, Space } from 'antd';

export function EntityDrawer({ open, title, description, loading, onClose, onSubmit, children, width = 520 }: {
  open: boolean; title: string; description?: string; loading?: boolean; onClose: () => void;
  onSubmit?: () => void; children: ReactNode; width?: number;
}) {
  return (
    <Drawer
      open={open} title={<div className="drawer-title"><strong>{title}</strong>{description && <span>{description}</span>}</div>}
      width={width} onClose={onClose} destroyOnHidden
      footer={onSubmit ? <div className="drawer-footer"><Space><Button onClick={onClose}>取消</Button><Button type="primary" loading={loading} onClick={onSubmit}>保存</Button></Space></div> : null}
    >{children}</Drawer>
  );
}

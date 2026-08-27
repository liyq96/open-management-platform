import type { ReactNode } from 'react';

export function FilterBar({ children, actions }: { children: ReactNode; actions?: ReactNode }) {
  return <div className="filter-bar"><div className="filter-bar__fields">{children}</div>{actions && <div className="filter-bar__actions">{actions}</div>}</div>;
}

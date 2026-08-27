import type { ReactNode } from 'react';

export function PageIntro({ title, description, extra }: { title: string; description: string; extra?: ReactNode }) {
  return (
    <header className="page-intro">
      <div><h1>{title}</h1><p>{description}</p></div>
      {extra && <div className="page-intro__extra">{extra}</div>}
    </header>
  );
}

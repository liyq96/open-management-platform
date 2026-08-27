import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp, ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { BrowserRouter } from 'react-router-dom';
import App from '@/app/App';
import '@/styles/global.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 20_000, retry: 1, refetchOnWindowFocus: false },
    mutations: { retry: 0 },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#356ae6', colorInfo: '#356ae6', colorSuccess: '#2c9a70',
          colorWarning: '#d58a2b', colorError: '#d95050', borderRadius: 10,
          colorBgLayout: '#f5f7fb', colorText: '#202534', colorTextSecondary: '#6c7486',
          colorBorderSecondary: '#edf0f5', fontFamily: "Inter, 'PingFang SC', 'Microsoft YaHei', sans-serif",
          controlHeight: 38,
        },
        components: {
          Button: { primaryShadow: 'none', borderRadius: 9 },
          Card: { borderRadiusLG: 14 },
          Drawer: { paddingLG: 24 },
          Menu: { itemBorderRadius: 9, itemHeight: 42, itemMarginInline: 10 },
          Table: { headerBg: '#fafbfc', headerColor: '#697184', rowHoverBg: '#f7f9fd' },
        },
      }}
    >
      <AntApp>
        <QueryClientProvider client={queryClient}>
          <BrowserRouter><App /></BrowserRouter>
        </QueryClientProvider>
      </AntApp>
    </ConfigProvider>
  </React.StrictMode>,
);

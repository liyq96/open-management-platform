import axios, { AxiosError } from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/store/auth';
import type { ApiResponse } from '@/types/api';

export class BusinessError extends Error {
  constructor(messageText: string, public readonly code?: string, public readonly requestId?: string) {
    super(messageText);
    this.name = 'BusinessError';
  }
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>;
    if (body && typeof body.code === 'string' && body.code !== '200') {
      void message.error(body.message || '请求未成功');
      throw new BusinessError(body.message || '请求未成功', body.code, body.requestId);
    }
    return response;
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clear();
      if (location.pathname !== '/login') location.assign('/login');
    }
    const body = error.response?.data;
    const text = body?.message || (error.code === 'ECONNABORTED' ? '请求超时，请稍后重试' : '服务暂时不可用');
    void message.error(text);
    return Promise.reject(new BusinessError(text, body?.code, body?.requestId));
  },
);

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await http.get<ApiResponse<T>>(url, { params });
  return response.data.data;
}

export async function post<T>(url: string, data?: unknown): Promise<T> {
  const response = await http.post<ApiResponse<T>>(url, data ?? {});
  return response.data.data;
}

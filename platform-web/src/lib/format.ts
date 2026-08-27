import dayjs from 'dayjs';
import type { EntityId } from '@/types/api';

export const DATE_TIME_DISPLAY_FORMAT = 'YYYY-MM-DD HH:mm:ss';

export const idKey = (id?: EntityId | null) => (id == null ? '' : String(id));
export const formatDateTime = (value?: string | null) => {
  if (!value) return '-';

  const dateTime = dayjs(value);
  return dateTime.isValid() ? dateTime.format(DATE_TIME_DISPLAY_FORMAT) : '-';
};
export const compactId = (id?: EntityId) => {
  const value = idKey(id);
  return value.length > 12 ? `${value.slice(0, 6)}…${value.slice(-4)}` : value || '—';
};

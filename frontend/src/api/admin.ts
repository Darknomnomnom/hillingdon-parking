import client from './client';
import type { AdminUser, Role } from '../types';

export const getUsers = () =>
  client.get<AdminUser[]>('/admin/users');

export const updateUserRole = (id: string, role: Role) =>
  client.patch<AdminUser>(`/admin/users/${id}/role`, { role });

import client from './client';
import type { AuthResponse, RegisterRequest } from '../types';

export const login = (email: string, password: string) =>
  client.post<AuthResponse>('/auth/login', { email, password });

export const register = (data: RegisterRequest) =>
  client.post<AuthResponse>('/auth/register', data);

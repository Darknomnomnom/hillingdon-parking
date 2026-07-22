import client from './client';
import type { Badge } from '../types';

export interface SubmitBadgeRequest {
  plate: string;
  badgeNumber: string;
  expiresAt: string;
  photo: File;
}

export const submitBadge = (data: SubmitBadgeRequest) => {
  const formData = new FormData();
  formData.append('plate', data.plate);
  formData.append('badgeNumber', data.badgeNumber);
  formData.append('expiresAt', data.expiresAt);
  formData.append('photo', data.photo);

  return client.post<Badge>('/badges/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const getMyBadges = () =>
  client.get<Badge[]>('/badges/my');

export const getPendingBadges = () =>
  client.get<Badge[]>('/badges/pending');

export const approveBadge = (id: string) =>
  client.patch<Badge>(`/badges/${id}/approve`);

export const rejectBadge = (id: string, reason: string) =>
  client.patch<Badge>(`/badges/${id}/reject`, { reason });

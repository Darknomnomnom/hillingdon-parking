import client from './client';
import type {
  KpiSummary,
  FloorBreakdownItem,
  HourlyTrendPoint,
  NoShowStats,
  UserDistribution,
} from '../types';

export const getKpis = () =>
  client.get<KpiSummary>('/dashboard/kpis');

export const getFloorBreakdown = () =>
  client.get<FloorBreakdownItem[]>('/bays/floors');

export const getHourlyTrends = () =>
  client.get<HourlyTrendPoint[]>('/dashboard/trends');

export const getNoShowStats = () =>
  client.get<NoShowStats>('/dashboard/no-shows');

export const getUserDistribution = () =>
  client.get<UserDistribution>('/dashboard/user-distribution');

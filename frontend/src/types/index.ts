export type Role = 'PATIENT' | 'STAFF' | 'ADMIN';

export interface AdminUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: Role;
  createdAt: string;
}

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'ARRIVED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

export type VisitType = 'OUTPATIENT' | 'PLANNED_ADMISSION' | 'OTHER';

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface Booking {
  id: string;
  confirmationCode: string;
  plate: string;
  status: BookingStatus;
  visitType: VisitType;
  appointmentTime: string;
  arrivalWindowStart: string;
  arrivalWindowEnd: string;
  notes?: string;
  createdAt: string;
  patientId: string;
  patientName: string;
  bayId?: string;
  bayNumber?: string;
  bayType?: string;
  floorName?: string;
}

export interface CreateBookingRequest {
  plate: string;
  visitType: VisitType;
  appointmentTime: string;
  needsAccessible: boolean;
  notes?: string;
}

export interface Occupancy {
  occupied: number;
  total: number;
}

export interface RevenueBreakdown {
  prebooked: number;
  driveIn: number;
  premium: number;
  total: number;
}

export interface CostSavings {
  prebookedAvgCost: number;
  driveInAvgCost: number;
  savingsMessage: string;
}

export interface KpiSummary {
  occupancy: Occupancy;
  specificNeeds: Occupancy;
  premiumParking: Occupancy;
  evCharging: Occupancy;
  premiumRevenueToday: number;
  totalRevenueToday: RevenueBreakdown;
  avgParkingDurationMinutes: number;
  avgDurationByZoneMinutes: Record<string, number>;
  parkingCostSavings: CostSavings;
}

export interface FloorBreakdownItem {
  floorNumber: number;
  floorName: string;
  occupied: number;
  total: number;
  percentFull: number;
  evTotal: number;
  evOccupied: number;
  vehiclesParked: number;
}

export interface HourlyTrendPoint {
  hourLabel: string;
  occupancyPercent: number;
  revenue: number;
}

export interface NoShowBucket {
  label: string;
  count: number;
}

export interface NoShowStats {
  todayCount: number;
  weekCount: number;
  byTimeOfDay: NoShowBucket[];
  byDayOfWeek: NoShowBucket[];
  insight: string;
}

export interface DistributionCategory {
  count: number;
  percent: number;
}

export interface UserDistribution {
  totalActive: number;
  prebookedPatients: DistributionCategory;
  driveInPatients: DistributionCategory;
  doctors: DistributionCategory;
  premiumParking: DistributionCategory;
  trendVsLastWeekPercent: number;
}

export type Role = 'PATIENT' | 'STAFF';

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

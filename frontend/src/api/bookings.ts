import client from './client';
import type { Booking, CreateBookingRequest } from '../types';

export const createBooking = (data: CreateBookingRequest) =>
  client.post<Booking>('/bookings', data);

export const getMyBookings = () =>
  client.get<Booking[]>('/bookings/my');

export const getAllBookings = () =>
  client.get<Booking[]>('/bookings');

export const cancelBooking = (id: string) =>
  client.patch<Booking>(`/bookings/${id}/cancel`);

export const markNoShow = (id: string) =>
  client.patch<Booking>(`/bookings/${id}/no-show`);

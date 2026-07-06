import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyBookings, cancelBooking } from '../api/bookings';
import type { Booking, BookingStatus, VisitType } from '../types';

const STATUS_STYLES: Record<BookingStatus, string> = {
  PENDING:   'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  ARRIVED:   'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-700',
  NO_SHOW:   'bg-orange-100 text-orange-800',
};

const VISIT_LABELS: Record<VisitType, string> = {
  OUTPATIENT:       'Outpatient',
  PLANNED_ADMISSION: 'Planned admission',
  OTHER:            'Other',
};

const CANCELLABLE: BookingStatus[] = ['PENDING', 'CONFIRMED'];

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState<string | null>(null);

  useEffect(() => {
    getMyBookings()
      .then(r => setBookings(r.data))
      .catch(() => setError('Failed to load bookings.'))
      .finally(() => setLoading(false));
  }, []);

  const handleCancel = async (id: string) => {
    if (!confirm('Cancel this booking?')) return;
    setCancelling(id);
    try {
      const { data } = await cancelBooking(id);
      setBookings(b => b.map(x => (x.id === id ? data : x)));
    } catch {
      alert('Could not cancel booking. Please try again.');
    } finally {
      setCancelling(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-65px)] flex items-center justify-center">
        <div className="text-gray-500 text-sm">Loading bookings…</div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">My bookings</h1>
          <p className="text-sm text-gray-500 mt-0.5">{bookings.length} booking{bookings.length !== 1 ? 's' : ''}</p>
        </div>
        <Link
          to="/book"
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
        >
          + New booking
        </Link>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
          {error}
        </div>
      )}

      {bookings.length === 0 && !error && (
        <div className="text-center py-20">
          <div className="w-14 h-14 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-7 h-7 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <p className="text-gray-600 font-medium">No bookings yet</p>
          <p className="text-sm text-gray-400 mt-1">Reserve a parking bay before your appointment</p>
          <Link
            to="/book"
            className="inline-block mt-4 bg-blue-600 text-white px-6 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
          >
            Book parking
          </Link>
        </div>
      )}

      <div className="space-y-4">
        {bookings.map(b => (
          <div key={b.id} className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-3 flex-wrap mb-3">
                  <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${STATUS_STYLES[b.status]}`}>
                    {b.status.replace('_', ' ')}
                  </span>
                  <span className="text-xs text-gray-400 font-mono">{b.confirmationCode}</span>
                </div>

                <div className="grid grid-cols-2 gap-x-6 gap-y-2">
                  <div>
                    <p className="text-xs text-gray-400 uppercase tracking-wide">Vehicle</p>
                    <p className="text-sm font-mono font-medium text-gray-900">{b.plate}</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-400 uppercase tracking-wide">Visit</p>
                    <p className="text-sm text-gray-900">{VISIT_LABELS[b.visitType]}</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-400 uppercase tracking-wide">Appointment</p>
                    <p className="text-sm text-gray-900">
                      {new Date(b.appointmentTime).toLocaleString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric',
                        hour: '2-digit', minute: '2-digit',
                      })}
                    </p>
                  </div>
                  {b.bayNumber && (
                    <div>
                      <p className="text-xs text-gray-400 uppercase tracking-wide">Bay</p>
                      <p className="text-sm font-semibold text-gray-900">
                        {b.bayNumber}
                        {b.floorName && <span className="font-normal text-gray-500"> · {b.floorName}</span>}
                      </p>
                    </div>
                  )}
                </div>
              </div>

              {CANCELLABLE.includes(b.status) && (
                <button
                  onClick={() => handleCancel(b.id)}
                  disabled={cancelling === b.id}
                  className="text-xs text-red-600 hover:text-red-700 border border-red-200 hover:border-red-300 px-3 py-1.5 rounded-lg disabled:opacity-50 shrink-0 transition-colors"
                >
                  {cancelling === b.id ? 'Cancelling…' : 'Cancel'}
                </button>
              )}
            </div>

            {(b.status === 'CONFIRMED' || b.status === 'PENDING') && (
              <div className="mt-4 pt-4 border-t border-gray-100 flex items-center gap-2 text-xs text-blue-700">
                <svg className="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Arrival window:{' '}
                {new Date(b.arrivalWindowStart).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
                {' – '}
                {new Date(b.arrivalWindowEnd).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

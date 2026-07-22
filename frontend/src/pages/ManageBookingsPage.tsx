import { useEffect, useState } from 'react';
import { getAllBookings, cancelBooking, markNoShow } from '../api/bookings';
import { simulateArrival } from '../api/anpr';
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

const STATUS_FILTERS: Array<BookingStatus | 'ALL'> = [
  'ALL', 'PENDING', 'CONFIRMED', 'ARRIVED', 'COMPLETED', 'CANCELLED', 'NO_SHOW',
];

export default function ManageBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState<string | null>(null);
  const [markingNoShow, setMarkingNoShow] = useState<string | null>(null);
  const [simulatingArrival, setSimulatingArrival] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<BookingStatus | 'ALL'>('ALL');

  const loadBookings = () =>
    getAllBookings()
      .then(r => setBookings(r.data))
      .catch(() => setError('Failed to load bookings.'));

  useEffect(() => {
    loadBookings().finally(() => setLoading(false));
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

  const handleMarkNoShow = async (id: string) => {
    if (!confirm('Mark this booking as a no-show now? Normally this happens automatically 30 minutes after the appointment time — this forces it immediately.')) return;
    setMarkingNoShow(id);
    try {
      const { data } = await markNoShow(id);
      setBookings(b => b.map(x => (x.id === id ? data : x)));
    } catch {
      alert('Could not mark booking as a no-show. Please try again.');
    } finally {
      setMarkingNoShow(null);
    }
  };

  const handleSimulateArrival = async (b: Booking) => {
    setSimulatingArrival(b.id);
    try {
      await simulateArrival(b.plate);
      await loadBookings();
    } catch {
      alert('Could not simulate arrival. Please try again.');
    } finally {
      setSimulatingArrival(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-65px)] flex items-center justify-center">
        <div className="text-gray-500 text-sm">Loading bookings…</div>
      </div>
    );
  }

  const visible = statusFilter === 'ALL' ? bookings : bookings.filter(b => b.status === statusFilter);

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <div className="mb-8">
        <h1 className="text-2xl font-semibold text-gray-900">Manage bookings</h1>
        <p className="text-sm text-gray-500 mt-0.5">{bookings.length} booking{bookings.length !== 1 ? 's' : ''} across all patients</p>
      </div>

      <div className="flex flex-wrap gap-2 mb-6">
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`text-xs font-medium px-3 py-1.5 rounded-full border transition-colors ${
              statusFilter === s
                ? 'bg-blue-600 border-blue-600 text-white'
                : 'bg-white border-gray-200 text-gray-600 hover:border-gray-300'
            }`}
          >
            {s === 'ALL' ? 'All' : s.replace('_', ' ')}
          </button>
        ))}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
          {error}
        </div>
      )}

      {visible.length === 0 && !error && (
        <div className="text-center py-20">
          <p className="text-gray-600 font-medium">No bookings found</p>
          <p className="text-sm text-gray-400 mt-1">Try a different status filter</p>
        </div>
      )}

      <div className="space-y-4">
        {visible.map(b => (
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
                    <p className="text-xs text-gray-400 uppercase tracking-wide">Patient</p>
                    <p className="text-sm font-medium text-gray-900">{b.patientName}</p>
                  </div>
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

              <div className="flex flex-col gap-2 shrink-0">
                {b.status === 'CONFIRMED' && (
                  <button
                    onClick={() => handleSimulateArrival(b)}
                    disabled={simulatingArrival === b.id}
                    className="text-xs text-green-600 hover:text-green-700 border border-green-200 hover:border-green-300 px-3 py-1.5 rounded-lg disabled:opacity-50 transition-colors"
                  >
                    {simulatingArrival === b.id ? 'Simulating…' : 'Simulate arrival'}
                  </button>
                )}
                {b.status === 'CONFIRMED' && (
                  <button
                    onClick={() => handleMarkNoShow(b.id)}
                    disabled={markingNoShow === b.id}
                    className="text-xs text-orange-600 hover:text-orange-700 border border-orange-200 hover:border-orange-300 px-3 py-1.5 rounded-lg disabled:opacity-50 transition-colors"
                  >
                    {markingNoShow === b.id ? 'Marking…' : 'Mark no-show'}
                  </button>
                )}
                {CANCELLABLE.includes(b.status) && (
                  <button
                    onClick={() => handleCancel(b.id)}
                    disabled={cancelling === b.id}
                    className="text-xs text-red-600 hover:text-red-700 border border-red-200 hover:border-red-300 px-3 py-1.5 rounded-lg disabled:opacity-50 transition-colors"
                  >
                    {cancelling === b.id ? 'Cancelling…' : 'Cancel'}
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

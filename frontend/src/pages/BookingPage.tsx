import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBooking } from '../api/bookings';
import type { Booking, VisitType } from '../types';

const VISIT_TYPE_LABELS: Record<VisitType, string> = {
  OUTPATIENT: 'Outpatient appointment',
  PLANNED_ADMISSION: 'Planned admission',
  OTHER: 'Other visit',
};

function localDatetimeToInstant(local: string) {
  return new Date(local).toISOString();
}

function minDatetime() {
  const d = new Date();
  d.setMinutes(d.getMinutes() + 30);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export default function BookingPage() {
  const [form, setForm] = useState({
    plate: '',
    visitType: 'OUTPATIENT' as VisitType,
    appointmentTime: '',
    needsAccessible: false,
    notes: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [confirmed, setConfirmed] = useState<Booking | null>(null);
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await createBooking({
        plate: form.plate.toUpperCase().replace(/\s/g, ''),
        visitType: form.visitType,
        appointmentTime: localDatetimeToInstant(form.appointmentTime),
        needsAccessible: form.needsAccessible,
        notes: form.notes || undefined,
      });
      setConfirmed(data);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg || 'Could not create booking. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (confirmed) {
    return (
      <div className="min-h-[calc(100vh-65px)] flex items-center justify-center px-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl border border-gray-200 p-8 shadow-sm text-center">
            <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-7 h-7 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-xl font-semibold text-gray-900 mb-1">Booking confirmed</h2>
            <p className="text-sm text-gray-500 mb-6">Your parking bay has been reserved</p>

            <div className="bg-gray-50 rounded-xl p-5 text-left space-y-3 mb-6">
              <div className="flex justify-between items-center">
                <span className="text-xs text-gray-500 uppercase tracking-wide">Confirmation code</span>
                <span className="font-mono font-semibold text-gray-900 text-sm">{confirmed.confirmationCode}</span>
              </div>
              {confirmed.bayNumber && (
                <div className="flex justify-between items-center">
                  <span className="text-xs text-gray-500 uppercase tracking-wide">Bay number</span>
                  <span className="font-semibold text-gray-900">{confirmed.bayNumber}</span>
                </div>
              )}
              {confirmed.floorName && (
                <div className="flex justify-between items-center">
                  <span className="text-xs text-gray-500 uppercase tracking-wide">Floor</span>
                  <span className="text-gray-900 text-sm">{confirmed.floorName}</span>
                </div>
              )}
              <div className="flex justify-between items-center">
                <span className="text-xs text-gray-500 uppercase tracking-wide">Vehicle</span>
                <span className="font-mono text-gray-900 text-sm">{confirmed.plate}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-gray-500 uppercase tracking-wide">Visit type</span>
                <span className="text-gray-900 text-sm">{VISIT_TYPE_LABELS[confirmed.visitType]}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-gray-500 uppercase tracking-wide">Appointment</span>
                <span className="text-gray-900 text-sm">
                  {new Date(confirmed.appointmentTime).toLocaleString('en-GB', {
                    day: '2-digit', month: 'short', year: 'numeric',
                    hour: '2-digit', minute: '2-digit',
                  })}
                </span>
              </div>
            </div>

            <div className="bg-blue-50 rounded-xl p-4 text-left mb-6">
              <p className="text-sm text-blue-800 font-medium mb-1">Arrival window</p>
              <p className="text-xs text-blue-600">
                {new Date(confirmed.arrivalWindowStart).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
                {' – '}
                {new Date(confirmed.arrivalWindowEnd).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
              </p>
              <p className="text-xs text-blue-600 mt-1">
                Arrive within this window. If you do not arrive, your bay will be released.
              </p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => navigate('/my-bookings')}
                className="flex-1 bg-blue-600 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
              >
                View my bookings
              </button>
              <button
                onClick={() => { setConfirmed(null); setForm({ plate: '', visitType: 'OUTPATIENT', appointmentTime: '', needsAccessible: false, notes: '' }); }}
                className="flex-1 border border-gray-300 text-gray-700 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
              >
                Book another
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-65px)] flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-semibold text-gray-900">Book parking</h1>
          <p className="text-sm text-gray-500 mt-1">Reserve your bay before you arrive</p>
        </div>

        <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-gray-200 p-8 space-y-6 shadow-sm">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
              {error}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Vehicle registration plate</label>
            <input
              type="text"
              required
              value={form.plate}
              onChange={e => setForm(f => ({ ...f, plate: e.target.value }))}
              className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm font-mono uppercase focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="e.g. LK21 ABC"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Visit type</label>
            <div className="space-y-2">
              {(Object.keys(VISIT_TYPE_LABELS) as VisitType[]).map(vt => (
                <label key={vt} className="flex items-center gap-3 cursor-pointer">
                  <input
                    type="radio"
                    name="visitType"
                    value={vt}
                    checked={form.visitType === vt}
                    onChange={() => setForm(f => ({ ...f, visitType: vt }))}
                    className="accent-blue-600"
                  />
                  <span className="text-sm text-gray-700">{VISIT_TYPE_LABELS[vt]}</span>
                </label>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Appointment date & time</label>
            <input
              type="datetime-local"
              required
              min={minDatetime()}
              value={form.appointmentTime}
              onChange={e => setForm(f => ({ ...f, appointmentTime: e.target.value }))}
              className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div className="border border-blue-200 bg-blue-50 rounded-xl p-4">
            <label className="flex items-start gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={form.needsAccessible}
                onChange={e => setForm(f => ({ ...f, needsAccessible: e.target.checked }))}
                className="mt-0.5 accent-blue-600"
              />
              <div>
                <p className="text-sm font-medium text-blue-900">I hold a valid Blue Badge</p>
                <p className="text-xs text-blue-700 mt-0.5">
                  You will be assigned an accessible bay. Staff will verify your badge on arrival.
                </p>
              </div>
            </label>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Additional notes <span className="text-gray-400 font-normal">(optional)</span>
            </label>
            <textarea
              value={form.notes}
              onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
              rows={2}
              className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              placeholder="e.g. travelling with a wheelchair, need wider bay"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {loading ? 'Reserving bay…' : 'Reserve parking bay'}
          </button>
        </form>
      </div>
    </div>
  );
}

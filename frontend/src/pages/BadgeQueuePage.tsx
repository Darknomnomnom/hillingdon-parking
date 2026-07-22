import { useEffect, useState } from 'react';
import { approveBadge, getPendingBadges, getReviewedBadges, rejectBadge } from '../api/badges';
import type { Badge } from '../types';

type Tab = 'pending' | 'reviewed';

export default function BadgeQueuePage() {
  const [tab, setTab] = useState<Tab>('pending');
  const [badges, setBadges] = useState<Badge[]>([]);
  const [reviewed, setReviewed] = useState<Badge[]>([]);
  const [reviewedLoaded, setReviewedLoaded] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [savingId, setSavingId] = useState<string | null>(null);
  const [reasonDrafts, setReasonDrafts] = useState<Record<string, string>>({});

  useEffect(() => {
    getPendingBadges()
      .then(r => setBadges(r.data))
      .catch(() => setError('Failed to load pending badges.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (tab !== 'reviewed' || reviewedLoaded) return;
    getReviewedBadges()
      .then(r => setReviewed(r.data))
      .catch(() => setError('Failed to load reviewed badges.'))
      .finally(() => setReviewedLoaded(true));
  }, [tab, reviewedLoaded]);

  const handleApprove = async (id: string) => {
    setSavingId(id);
    setError('');
    try {
      const { data } = await approveBadge(id);
      setBadges(list => list.filter(b => b.id !== id));
      setReviewed(list => [data, ...list]);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg || 'Could not approve badge.');
    } finally {
      setSavingId(null);
    }
  };

  const handleReject = async (id: string) => {
    const reason = (reasonDrafts[id] || '').trim();
    if (!reason) {
      setError('A reason is required to reject a badge.');
      return;
    }
    setSavingId(id);
    setError('');
    try {
      const { data } = await rejectBadge(id, reason);
      setBadges(list => list.filter(b => b.id !== id));
      setReviewed(list => [data, ...list]);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg || 'Could not reject badge.');
    } finally {
      setSavingId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-65px)] flex items-center justify-center">
        <div className="text-gray-500 text-sm">Loading badge submissions…</div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <div className="mb-8">
        <h1 className="text-2xl font-semibold text-gray-900">Blue Badge verification</h1>
        <p className="text-sm text-gray-500 mt-0.5">
          {badges.length} badge{badges.length !== 1 ? 's' : ''} awaiting review
        </p>
      </div>

      <div className="flex gap-2 mb-6">
        <button
          onClick={() => setTab('pending')}
          className={`text-xs font-medium px-3 py-1.5 rounded-full border transition-colors ${
            tab === 'pending' ? 'bg-blue-600 border-blue-600 text-white' : 'bg-white border-gray-200 text-gray-600 hover:border-gray-300'
          }`}
        >
          Pending ({badges.length})
        </button>
        <button
          onClick={() => setTab('reviewed')}
          className={`text-xs font-medium px-3 py-1.5 rounded-full border transition-colors ${
            tab === 'reviewed' ? 'bg-blue-600 border-blue-600 text-white' : 'bg-white border-gray-200 text-gray-600 hover:border-gray-300'
          }`}
        >
          Reviewed
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
          {error}
        </div>
      )}

      {tab === 'reviewed' ? (
        !reviewedLoaded ? (
          <div className="text-sm text-gray-500 py-10 text-center">Loading reviewed badges…</div>
        ) : reviewed.length === 0 ? (
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm px-6 py-10 text-center text-sm text-gray-500">
            No badges have been reviewed yet.
          </div>
        ) : (
          <div className="space-y-4">
            {reviewed.map(badge => (
              <div key={badge.id} className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="flex flex-col sm:flex-row">
                  <div className="sm:w-56 shrink-0 bg-gray-100">
                    <img
                      src={badge.photoUrl}
                      alt={`Blue Badge ${badge.badgeNumber}`}
                      className="w-full h-48 sm:h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 p-6">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="font-medium text-gray-900">{badge.patientName || 'Unknown patient'}</div>
                        <div className="text-sm text-gray-500">{badge.patientEmail}</div>
                      </div>
                      <span className={`text-xs font-medium px-2.5 py-1 rounded-full shrink-0 ${
                        badge.status === 'VERIFIED' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-700'
                      }`}>
                        {badge.status}
                      </span>
                    </div>

                    <dl className="grid grid-cols-2 gap-x-4 gap-y-2 mt-4 text-sm">
                      <div>
                        <dt className="text-gray-400 text-xs uppercase tracking-wide">Badge number</dt>
                        <dd className="text-gray-900">{badge.badgeNumber}</dd>
                      </div>
                      <div>
                        <dt className="text-gray-400 text-xs uppercase tracking-wide">Vehicle plate</dt>
                        <dd className="text-gray-900">{badge.plate || '—'}</dd>
                      </div>
                      <div>
                        <dt className="text-gray-400 text-xs uppercase tracking-wide">Reviewed by</dt>
                        <dd className="text-gray-900">{badge.verifiedByName || '—'}</dd>
                      </div>
                      <div>
                        <dt className="text-gray-400 text-xs uppercase tracking-wide">Reviewed on</dt>
                        <dd className="text-gray-900">
                          {badge.verifiedAt
                            ? new Date(badge.verifiedAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
                            : '—'}
                        </dd>
                      </div>
                      {badge.status === 'REJECTED' && badge.rejectionReason && (
                        <div className="col-span-2">
                          <dt className="text-gray-400 text-xs uppercase tracking-wide">Rejection reason</dt>
                          <dd className="text-gray-900">{badge.rejectionReason}</dd>
                        </div>
                      )}
                    </dl>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )
      ) : badges.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm px-6 py-10 text-center text-sm text-gray-500">
          No badges are waiting for review.
        </div>
      ) : (
        <div className="space-y-4">
          {badges.map(badge => (
            <div key={badge.id} className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
              <div className="flex flex-col sm:flex-row">
                <div className="sm:w-56 shrink-0 bg-gray-100">
                  <img
                    src={badge.photoUrl}
                    alt={`Blue Badge ${badge.badgeNumber}`}
                    className="w-full h-48 sm:h-full object-cover"
                  />
                </div>
                <div className="flex-1 p-6">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <div className="font-medium text-gray-900">{badge.patientName || 'Unknown patient'}</div>
                      <div className="text-sm text-gray-500">{badge.patientEmail}</div>
                    </div>
                    <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-amber-100 text-amber-800 shrink-0">
                      PENDING
                    </span>
                  </div>

                  <dl className="grid grid-cols-2 gap-x-4 gap-y-2 mt-4 text-sm">
                    <div>
                      <dt className="text-gray-400 text-xs uppercase tracking-wide">Badge number</dt>
                      <dd className="text-gray-900">{badge.badgeNumber}</dd>
                    </div>
                    <div>
                      <dt className="text-gray-400 text-xs uppercase tracking-wide">Vehicle plate</dt>
                      <dd className="text-gray-900">{badge.plate || '—'}</dd>
                    </div>
                    <div>
                      <dt className="text-gray-400 text-xs uppercase tracking-wide">Expires</dt>
                      <dd className="text-gray-900">
                        {new Date(badge.expiresAt).toLocaleDateString('en-GB', {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric',
                        })}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-gray-400 text-xs uppercase tracking-wide">Submitted</dt>
                      <dd className="text-gray-900">
                        {new Date(badge.createdAt).toLocaleDateString('en-GB', {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric',
                        })}
                      </dd>
                    </div>
                  </dl>

                  <div className="mt-5 flex flex-col sm:flex-row gap-3">
                    <button
                      onClick={() => handleApprove(badge.id)}
                      disabled={savingId === badge.id}
                      className="text-sm bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 disabled:opacity-40 disabled:cursor-not-allowed font-medium"
                    >
                      Approve
                    </button>
                    <input
                      type="text"
                      placeholder="Reason for rejection"
                      value={reasonDrafts[badge.id] || ''}
                      onChange={e =>
                        setReasonDrafts(drafts => ({ ...drafts, [badge.id]: e.target.value }))
                      }
                      className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                      onClick={() => handleReject(badge.id)}
                      disabled={savingId === badge.id}
                      className="text-sm bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 disabled:opacity-40 disabled:cursor-not-allowed font-medium"
                    >
                      Reject
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

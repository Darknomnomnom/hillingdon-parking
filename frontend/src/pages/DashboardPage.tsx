import { useEffect, useState } from 'react';
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer,
} from 'recharts';
import type { ValueType, NameType } from 'recharts/types/component/DefaultTooltipContent';
import {
  getKpis, getFloorBreakdown, getHourlyTrends, getNoShowStats, getUserDistribution,
} from '../api/dashboard';
import type {
  KpiSummary, FloorBreakdownItem, HourlyTrendPoint, NoShowStats, UserDistribution,
} from '../types';

const GREEN = '#22C55E';
const AMBER = '#F59E0B';
const ORANGE = '#F97316';
const RED = '#EF4444';
const PURPLE = '#8B5CF6';
const BLUE = '#3B82F6';

function formatGbp(value: number) {
  return new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' }).format(value);
}

function KpiCard({
  title, occupied, total, color, suffix,
}: { title: string; occupied: number; total: number; color: string; suffix?: string }) {
  const percent = total === 0 ? 0 : Math.round((occupied / total) * 100);
  return (
    <div className="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
      <p className="text-xs text-gray-400 uppercase tracking-wide">{title}</p>
      <p className="text-2xl font-semibold text-gray-900 mt-1">
        {occupied}/{total}{suffix}
      </p>
      <div className="w-full bg-gray-100 rounded-full h-2 mt-3">
        <div
          className="h-2 rounded-full"
          style={{ width: `${percent}%`, backgroundColor: color }}
        />
      </div>
      <p className="text-xs text-gray-500 mt-1.5">{percent}% full</p>
    </div>
  );
}

function StatCard({ title, value, subtitle }: { title: string; value: string; subtitle?: string }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
      <p className="text-xs text-gray-400 uppercase tracking-wide">{title}</p>
      <p className="text-2xl font-semibold text-gray-900 mt-1">{value}</p>
      {subtitle && <p className="text-xs text-gray-500 mt-1.5">{subtitle}</p>}
    </div>
  );
}

function floorBadgeColor(percentFull: number) {
  if (percentFull >= 90) return { bg: 'bg-red-100', text: 'text-red-700', dot: RED };
  if (percentFull >= 75) return { bg: 'bg-amber-100', text: 'text-amber-700', dot: AMBER };
  return { bg: 'bg-green-100', text: 'text-green-700', dot: GREEN };
}

function FloorRow({ floor }: { floor: FloorBreakdownItem }) {
  const [open, setOpen] = useState(false);
  const colors = floorBadgeColor(floor.percentFull);
  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-sm">
      <button
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center justify-between px-5 py-4 text-left"
      >
        <div className="flex items-center gap-3">
          <span className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold ${colors.bg} ${colors.text}`}>
            {floor.floorNumber}
          </span>
          <div>
            <p className="text-sm font-medium text-gray-900">{floor.floorName}</p>
            <p className="text-xs text-gray-500">{floor.occupied}/{floor.total} occupied · {Math.round(floor.percentFull)}%</p>
          </div>
        </div>
        <svg className={`w-4 h-4 text-gray-400 transition-transform ${open ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      {open && (
        <div className="px-5 pb-4 pt-0 grid grid-cols-2 gap-4 border-t border-gray-100 mt-1">
          <div className="pt-3">
            <p className="text-xs text-gray-400 uppercase tracking-wide">EV chargers</p>
            <p className="text-sm text-gray-900 mt-0.5">{floor.evOccupied}/{floor.evTotal} in use</p>
          </div>
          <div className="pt-3">
            <p className="text-xs text-gray-400 uppercase tracking-wide">Vehicles parked</p>
            <p className="text-sm text-gray-900 mt-0.5">{floor.vehiclesParked}</p>
          </div>
        </div>
      )}
    </div>
  );
}

function DistributionBar({
  label, count, percent, color,
}: { label: string; count: number; percent: number; color: string }) {
  return (
    <div>
      <div className="flex items-center justify-between text-sm mb-1">
        <span className="flex items-center gap-2 text-gray-700">
          <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: color }} />
          {label}
        </span>
        <span className="text-gray-500">{count} · {Math.round(percent)}%</span>
      </div>
      <div className="w-full bg-gray-100 rounded-full h-2">
        <div className="h-2 rounded-full" style={{ width: `${percent}%`, backgroundColor: color }} />
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const [kpis, setKpis] = useState<KpiSummary | null>(null);
  const [floors, setFloors] = useState<FloorBreakdownItem[]>([]);
  const [trends, setTrends] = useState<HourlyTrendPoint[]>([]);
  const [noShows, setNoShows] = useState<NoShowStats | null>(null);
  const [distribution, setDistribution] = useState<UserDistribution | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      getKpis(), getFloorBreakdown(), getHourlyTrends(), getNoShowStats(), getUserDistribution(),
    ])
      .then(([k, f, t, n, d]) => {
        setKpis(k.data);
        setFloors(f.data);
        setTrends(t.data);
        setNoShows(n.data);
        setDistribution(d.data);
      })
      .catch(() => setError('Failed to load dashboard data.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-65px)] flex items-center justify-center">
        <div className="text-gray-500 text-sm">Loading dashboard…</div>
      </div>
    );
  }

  if (error || !kpis || !noShows || !distribution) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-10">
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
          {error || 'Could not load dashboard data.'}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-10 space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">Staff dashboard</h1>
        <p className="text-sm text-gray-500 mt-0.5">Live parking occupancy and revenue overview</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard title="Multi-storey occupancy" occupied={kpis.occupancy.occupied} total={kpis.occupancy.total} color={GREEN} />
        <KpiCard title="Specific needs parking" occupied={kpis.specificNeeds.occupied} total={kpis.specificNeeds.total} color={AMBER} />
        <KpiCard title="Premium parking" occupied={kpis.premiumParking.occupied} total={kpis.premiumParking.total} color={ORANGE} />
        <KpiCard title="EV charging stations" occupied={kpis.evCharging.occupied} total={kpis.evCharging.total} color={PURPLE} />
        <StatCard
          title="Premium parking revenue"
          value={formatGbp(kpis.premiumRevenueToday)}
          subtitle="Estimated, today"
        />
        <StatCard
          title="Total revenue (today)"
          value={formatGbp(kpis.totalRevenueToday.total)}
          subtitle={`Pre-booked ${formatGbp(kpis.totalRevenueToday.prebooked)} · Drive-in ${formatGbp(kpis.totalRevenueToday.driveIn)} · Premium ${formatGbp(kpis.totalRevenueToday.premium)}`}
        />
        <StatCard
          title="Avg. parking duration"
          value={`${Math.round(kpis.avgParkingDurationMinutes)} min`}
          subtitle={Object.entries(kpis.avgDurationByZoneMinutes).map(([z, m]) => `${z}: ${Math.round(m)}m`).join(' · ') || 'No active sessions'}
        />
        <StatCard
          title="Parking costs"
          value={`${formatGbp(kpis.parkingCostSavings.prebookedAvgCost)} vs ${formatGbp(kpis.parkingCostSavings.driveInAvgCost)}`}
          subtitle={kpis.parkingCostSavings.savingsMessage}
        />
      </div>

      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-3">Floor-by-floor breakdown</h2>
        <div className="space-y-3">
          {floors.map(f => <FloorRow key={f.floorNumber} floor={f} />)}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">User distribution</h2>
            <span className={`text-xs font-medium ${distribution.trendVsLastWeekPercent >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {distribution.trendVsLastWeekPercent >= 0 ? '↑' : '↓'} {Math.abs(Math.round(distribution.trendVsLastWeekPercent))}% vs last week
            </span>
          </div>
          <p className="text-sm text-gray-500 mb-4">{distribution.totalActive} total active users</p>
          <div className="space-y-4">
            <DistributionBar label="Pre-booked patients" count={distribution.prebookedPatients.count} percent={distribution.prebookedPatients.percent} color={BLUE} />
            <DistributionBar label="Drive-in patients" count={distribution.driveInPatients.count} percent={distribution.driveInPatients.percent} color={ORANGE} />
            <DistributionBar label="Doctors" count={distribution.doctors.count} percent={distribution.doctors.percent} color={PURPLE} />
            <DistributionBar label="Premium parking" count={distribution.premiumParking.count} percent={distribution.premiumParking.percent} color={AMBER} />
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Pre-booking no-shows</h2>
            <div className="text-right text-xs text-gray-500">
              <p>Today: <span className="font-semibold text-gray-900">{noShows.todayCount}</span></p>
              <p>This week: <span className="font-semibold text-gray-900">{noShows.weekCount}</span></p>
            </div>
          </div>
          <p className="text-xs text-gray-400 uppercase tracking-wide mb-2">By time of day</p>
          <ResponsiveContainer width="100%" height={140}>
            <BarChart data={noShows.byTimeOfDay}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
              <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="count" name="No-shows" fill={ORANGE} radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
          <p className="text-xs text-gray-400 uppercase tracking-wide mb-2 mt-4">By day of week</p>
          <ResponsiveContainer width="100%" height={140}>
            <BarChart data={noShows.byDayOfWeek}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
              <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="count" name="No-shows" fill={RED} radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
          <div className="mt-4 pt-3 border-t border-gray-100 text-xs text-gray-500">
            {noShows.insight}
          </div>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Hourly trends (last 12 hours)</h2>
        <ResponsiveContainer width="100%" height={280}>
          <AreaChart data={trends}>
            <defs>
              <linearGradient id="occupancyFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={BLUE} stopOpacity={0.3} />
                <stop offset="95%" stopColor={BLUE} stopOpacity={0} />
              </linearGradient>
              <linearGradient id="revenueFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={GREEN} stopOpacity={0.3} />
                <stop offset="95%" stopColor={GREEN} stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
            <XAxis dataKey="hourLabel" tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} />
            <YAxis yAxisId="left" tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} unit="%" />
            <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} unit="£" />
            <Tooltip formatter={(value: ValueType = 0, name: NameType = '') => (name === 'Revenue' ? formatGbp(Number(value)) : `${Math.round(Number(value))}%`)} />
            <Legend wrapperStyle={{ fontSize: 12 }} />
            <Area yAxisId="left" type="monotone" dataKey="occupancyPercent" name="Occupancy %" stroke={BLUE} fill="url(#occupancyFill)" strokeWidth={2} />
            <Area yAxisId="right" type="monotone" dataKey="revenue" name="Revenue" stroke={GREEN} fill="url(#revenueFill)" strokeWidth={2} />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

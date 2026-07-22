import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BookingPage from './pages/BookingPage';
import MyBookingsPage from './pages/MyBookingsPage';
import DashboardPage from './pages/DashboardPage';
import ManageBookingsPage from './pages/ManageBookingsPage';
import AdminPage from './pages/AdminPage';
import BadgeQueuePage from './pages/BadgeQueuePage';
import type { ReactNode } from 'react';

function homeFor(role: string | undefined) {
  return role === 'STAFF' || role === 'ADMIN' ? '/dashboard' : '/my-bookings';
}

function PatientRoute({ children }: { children: ReactNode }) {
  const { token, user } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  return user?.role === 'PATIENT' ? <>{children}</> : <Navigate to={homeFor(user?.role)} replace />;
}

function GuestRoute({ children }: { children: ReactNode }) {
  const { token, user } = useAuth();
  return !token ? <>{children}</> : <Navigate to={homeFor(user?.role)} replace />;
}

function StaffRoute({ children }: { children: ReactNode }) {
  const { token, user } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  return user?.role === 'STAFF' || user?.role === 'ADMIN' ? <>{children}</> : <Navigate to={homeFor(user?.role)} replace />;
}

function AdminRoute({ children }: { children: ReactNode }) {
  const { token, user } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  return user?.role === 'ADMIN' ? <>{children}</> : <Navigate to={homeFor(user?.role)} replace />;
}

function AppRoutes() {
  const { user } = useAuth();
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <Routes>
        <Route path="/" element={<Navigate to={homeFor(user?.role)} replace />} />
        <Route path="/login" element={<GuestRoute><LoginPage /></GuestRoute>} />
        <Route path="/register" element={<GuestRoute><RegisterPage /></GuestRoute>} />
        <Route path="/book" element={<PatientRoute><BookingPage /></PatientRoute>} />
        <Route path="/my-bookings" element={<PatientRoute><MyBookingsPage /></PatientRoute>} />
        <Route path="/dashboard" element={<StaffRoute><DashboardPage /></StaffRoute>} />
        <Route path="/bookings" element={<StaffRoute><ManageBookingsPage /></StaffRoute>} />
        <Route path="/badges" element={<StaffRoute><BadgeQueuePage /></StaffRoute>} />
        <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}

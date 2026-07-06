import { createContext, useContext, useState, type ReactNode } from 'react';
import type { AuthResponse } from '../types';

interface StoredUser {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'PATIENT' | 'STAFF';
}

interface AuthContextValue {
  user: StoredUser | null;
  token: string | null;
  signIn: (resp: AuthResponse) => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [user, setUser] = useState<StoredUser | null>(() => {
    const s = localStorage.getItem('user');
    return s ? JSON.parse(s) : null;
  });

  const signIn = (resp: AuthResponse) => {
    const u: StoredUser = {
      userId: resp.userId,
      email: resp.email,
      firstName: resp.firstName,
      lastName: resp.lastName,
      role: resp.role,
    };
    setToken(resp.token);
    setUser(u);
    localStorage.setItem('token', resp.token);
    localStorage.setItem('user', JSON.stringify(u));
  };

  const signOut = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ user, token, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}

import axios from 'axios';

// Local dev: Vite proxies '/api' -> localhost:8080 (see vite.config.ts), so this stays relative.
// Production (Vercel): VITE_API_URL points at the Railway backend origin, since there's no dev proxy
// once this is a static build — the backend's CORS_ALLOWED_ORIGINS must include the Vercel domain.
const apiUrl = import.meta.env.VITE_API_URL;
const client = axios.create({
  baseURL: apiUrl ? `${apiUrl}/api` : '/api',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default client;

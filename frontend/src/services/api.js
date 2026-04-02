import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

// Add a request interceptor to include JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  console.log('[API] Request:', config.method?.toUpperCase(), config.url, { hasToken: !!token });
  return config;
}, (error) => {
  console.error('[API] Request error:', error);
  return Promise.reject(error);
});

// Add a response interceptor to handle global errors (like 401 Unauthorized)
api.interceptors.response.use(
  (response) => {
    console.log('[API] Response success:', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error('[API] Response error:', error.response?.status, error.response?.data?.message || error.message);
    
    if (error.response && error.response.status === 401) {
      // Token expired or invalid
      console.warn('[API] Unauthorized (401) - Clearing token and redirecting to login');
      localStorage.removeItem('token');
      localStorage.removeItem('roles');
      localStorage.removeItem('username');
      localStorage.removeItem('pendingUsername');
      // Redirect to login page
      window.location.href = '/';
    } else if (error.response && error.response.status === 403) {
      console.warn('[API] Forbidden (403) - Access denied');
    }
    return Promise.reject(error);
  }
);

export default api;

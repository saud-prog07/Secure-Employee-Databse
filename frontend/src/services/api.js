import axios from 'axios';
import { storage } from '../utils/storage';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

// Add a request interceptor to include JWT token
api.interceptors.request.use((config) => {
  const token = storage.get('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Add a response interceptor to handle global errors (like 401 Unauthorized)
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    
    if (error.response && error.response.status === 401) {
      // Token expired or invalid
      storage.clearAuth();
      // Redirect to login page
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default api;

import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { FaUser, FaLock } from 'react-icons/fa';
import api from '../services/api';
import { storage } from '../utils/storage';
import './Login.css';

const Login = () => {
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const [rememberMe, setRememberMe] = useState(false);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
    const navigate = useNavigate();

    // Helper for role-based redirection
    const redirectByRole = useCallback((roles) => {
        if (!roles || roles.length === 0) {
            console.warn('[Login] No roles found for redirection');
            setIsCheckingAuth(false);
            return;
        }

        const cleanRoles = roles.map(role => role.toString().replace('ROLE_', '').toUpperCase());
        console.log('[Login] Processing redirect for roles:', cleanRoles);
        
        if (cleanRoles.includes('ADMIN') || cleanRoles.includes('HR')) {
            console.log('[Login] Navigating to /dashboard');
            navigate('/dashboard', { replace: true });
        } else if (cleanRoles.includes('EMPLOYEE')) {
            console.log('[Login] Navigating to /attendance-scan');
            navigate('/attendance-scan', { replace: true });
        } else {
            console.warn('[Login] Role not recognized, falling back to /attendance-scan');
            navigate('/attendance-scan', { replace: true });
        }
    }, [navigate]);

    // Check for existing session on mount
    useEffect(() => {
        const token = storage.get('token');
        const rolesString = storage.get('roles');
        
        console.log('[Login] Checking auth on mount...', { hasToken: !!token, hasRoles: !!rolesString });

        if (token && rolesString) {
            try {
                const roles = JSON.parse(rolesString);
                console.log('[Login] Active session found, redirecting...');
                redirectByRole(roles);
                return; // Early return, don't stop the spinner
            } catch (e) {
                console.error('[Login] Session data corrupted:', e);
                storage.clearAuth();
            }
        }
        
        console.log('[Login] No session found, showing login form');
        setIsCheckingAuth(false);
    }, [redirectByRole]);

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            const response = await api.post('/api/auth/login', { identifier, password });
            const responseData = response.data.data;
            
            if (responseData.status === 'OTP_REQUIRED') {
                storage.set('pendingUsername', identifier, rememberMe);
                navigate('/verify-otp');
            } else if (responseData.status === 'SUCCESS') {
                const token = responseData.token;
                const roles = responseData.roles || [];
                const username = responseData.username || identifier;

                // Save to storage (conditional based on rememberMe)
                storage.set('token', token, rememberMe);
                storage.set('roles', roles, rememberMe);
                storage.set('username', username, rememberMe);
                
                // Clear any guest flags
                storage.remove('guestMode');
                
                redirectByRole(roles);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid credentials');
        } finally {
            setLoading(false);
        }
    };

    if (isCheckingAuth) {
        return (
            <div className="login-bg-container">
                <div className="login-glass-card">
                    <div className="loading-spinner"></div>
                    <p style={{ color: 'white', marginTop: '20px' }}>Checking session...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="login-bg-container">
            <div className="login-glass-card">
                <h1 className="login-title">Login</h1>
                
                <form onSubmit={handleLogin} className="login-form-content">
                    <div className="input-pill-container">
                        <input 
                            type="text" 
                            placeholder="Username" 
                            value={identifier} 
                            onChange={(e) => setIdentifier(e.target.value)}
                            required
                        />
                        <FaUser className="pill-icon" />
                    </div>
                    
                    <div className="input-pill-container">
                        <input 
                            type="password" 
                            placeholder="Password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <FaLock className="pill-icon" />
                    </div>

                    <div className="login-options-row">
                        <div className="remember-me-group">
                            <input 
                                type="checkbox" 
                                id="remember" 
                                checked={rememberMe}
                                onChange={(e) => setRememberMe(e.target.checked)}
                            /> 
                            <label htmlFor="remember">Remember me</label>
                        </div>
                        <Link to="/forgot-password" value="Forgot password?" className="forgot-link">Forgot password?</Link>
                    </div>
                    
                    <button 
                        className="login-pill-button" 
                        type="submit" 
                        disabled={loading}
                    >
                        {loading ? 'Please wait...' : 'Login'}
                    </button>
                    
                    <div className="register-footer">
                        Don't have an account? <Link to="/register">Register</Link>
                    </div>

                    {error && (
                        <div className="status-error-msg">
                            {error}
                        </div>
                    )}
                </form>

                {/* Subtle Demo Info */}
                <div className="mini-demo-hint">
                    admin/password123 | emp001/emp123
                </div>
            </div>
        </div>
    );
};

export default Login;

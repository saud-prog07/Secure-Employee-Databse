import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

const Login = () => {
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        
        console.log('[Login] Attempting login with identifier:', identifier);
        
        try {
            const response = await api.post('/api/auth/login', { identifier, password });
            
            console.log('[Login] Response:', response.data);
            
            // Validate response structure
            if (!response.data || !response.data.data) {
                console.error('[Login] Invalid response structure');
                setError('Invalid server response. Please try again.');
                return;
            }
            
            const responseData = response.data.data;
            
            // Check if OTP is required (2FA enabled)
            if (responseData.status === 'OTP_REQUIRED') {
                console.log('[Login] 2FA enabled - redirecting to OTP verification');
                localStorage.setItem('pendingUsername', identifier);
                navigate('/verify-otp');
            } else if (responseData.status === 'SUCCESS') {
                console.log('[Login] 2FA disabled - storing token');
                console.log('[Login] Token:', responseData.token ? 'Present' : 'Missing');
                console.log('[Login] Roles:', responseData.roles);
                
                if (!responseData.token) {
                    console.error('[Login] Token missing from response');
                    setError('Login failed: No token received. Please try again.');
                    return;
                }
                
                localStorage.setItem('token', responseData.token);
                localStorage.setItem('roles', JSON.stringify(responseData.roles || []));
                localStorage.setItem('username', responseData.username || email);
                localStorage.removeItem('guestMode');
                
                console.log('[Login] Token stored in localStorage');
                
                // Role-based redirection
                const roles = responseData.roles || [];
                console.log('[Login] User roles:', roles);
                
                // Check if user has ADMIN or HR role
                const isAdminOrHR = roles.some(role => 
                    role.includes('ADMIN') || role.includes('HR')
                );
                
                if (isAdminOrHR) {
                    console.log('[Login] User is ADMIN/HR - redirecting to dashboard');
                    navigate('/dashboard', { replace: true });
                } else {
                    console.log('[Login] User is EMPLOYEE - redirecting to attendance-scan');
                    navigate('/attendance-scan', { replace: true });
                }
            } else {
                console.error('[Login] Unknown response status:', responseData.status);
                setError('Unexpected server response. Please try again.');
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || 'Login failed - Check credentials';
            console.error('[Login] Error:', errorMessage, err);
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ 
            height: '100vh', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center', 
            background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)' 
        }}>
            <div className="card" style={{ width: '100%', maxWidth: '420px', padding: '2.5rem' }}>
                {/* Logo and Title */}
                <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
                    <div style={{ 
                        width: '60px', 
                        height: '60px', 
                        backgroundColor: 'var(--primary)', 
                        borderRadius: '12px', 
                        margin: '0 auto 1rem auto',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: '0 10px 15px -3px rgba(79, 70, 229, 0.4)',
                        fontSize: '1.8rem'
                    }}>
                        ðŸ¢
                    </div>
                    <h1 style={{ margin: '0 0 0.5rem 0', fontSize: '1.75rem', color: 'var(--text-primary)' }}>
                        EMS.io
                    </h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', margin: 0 }}>
                        Employee Management System
                    </p>
                </div>

                {/* Login Form */}
                <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    {/* Email/Username Input */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                            Email or Username
                        </label>
                        <input 
                            type="text" 
                            placeholder="admin or emp001" 
                            value={identifier} 
                            onChange={(e) => setIdentifier(e.target.value)}
                            required
                            autoFocus
                            style={{ width: '100%' }}
                        />
                    </div>
                    
                    {/* Password Input */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                            Password
                        </label>
                        <input 
                            type="password" 
                            placeholder="Enter your password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{ width: '100%' }}
                        />
                    </div>

                    {/* Error Message */}
                    {error && (
                        <div style={{ 
                            fontSize: '0.875rem', 
                            color: 'var(--danger)', 
                            backgroundColor: '#fee2e2', 
                            padding: '0.75rem', 
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            {error}
                        </div>
                    )}

                    {/* Sign In Button */}
                    <button 
                        className="btn-primary" 
                        type="submit" 
                        disabled={loading}
                        style={{ width: '100%', justifyContent: 'center', padding: '0.875rem', marginTop: '0.5rem' }}
                    >
                        {loading ? 'Signing in...' : 'Sign In'}
                    </button>
                </form>

                {/* Demo Credentials Info */}
                <div style={{
                    marginTop: '1.5rem',
                    padding: '1rem',
                    backgroundColor: '#f0f9ff',
                    borderRadius: '8px',
                    border: '1px solid #bae6fd',
                    fontSize: '0.8rem',
                    color: '#0369a1',
                    textAlign: 'center'
                }}>
                    <strong>Demo Credentials:</strong><br/>
                    <code>admin</code> / <code>password123</code> (Admin/HR)<br/>
                    <code>emp001</code> / <code>emp123</code> (Employee)
                </div>
            </div>
        </div>
    );
};

export default Login;

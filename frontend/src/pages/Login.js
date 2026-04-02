import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';

const Login = () => {
    const [loginMode, setLoginMode] = useState('choice'); // 'choice', 'admin', 'employee'
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        
        console.log('[Login] Attempting login with username:', username, 'mode:', loginMode);
        
        try {
            const response = await api.post('/api/auth/login', { username, password });
            
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
                localStorage.setItem('pendingUsername', username);
                localStorage.setItem('loginMode', loginMode); // Store which mode to return to
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
                localStorage.setItem('username', responseData.username || username);
                localStorage.removeItem('guestMode');
                
                console.log('[Login] Token stored in localStorage');
                
                // Route based on login mode
                if (loginMode === 'employee') {
                    console.log('[Login] Employee mode - redirecting to attendance-scan');
                    navigate('/attendance-scan');
                } else {
                    console.log('[Login] Admin mode - redirecting to dashboard');
                    navigate('/dashboard');
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

    const handleBack = () => {
        setLoginMode('choice');
        setUsername('');
        setPassword('');
        setError(null);
    };

    return (
        <div style={{ 
            height: '100vh', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center', 
            background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)' 
        }}>
            <div className="card" style={{ width: '100%', maxWidth: '450px', padding: '2.5rem' }}>
                {/* Choice Screen */}
                {loginMode === 'choice' && (
                    <>
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
                                fontSize: '1.5rem'
                            }}>
                                🏢
                            </div>
                            <h2 style={{ margin: '0 0 0.5rem 0' }}>Select Login Type</h2>
                            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', margin: 0 }}>
                                Choose your role to continue
                            </p>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                            {/* Admin Login Button */}
                            <button 
                                type="button"
                                onClick={() => setLoginMode('admin')}
                                style={{
                                    width: '100%',
                                    padding: '1.5rem',
                                    border: '2px solid #e2e8f0',
                                    borderRadius: '12px',
                                    backgroundColor: '#fff',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '1rem',
                                    transition: 'all 0.3s ease',
                                    textAlign: 'left'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.borderColor = 'var(--primary)';
                                    e.currentTarget.style.backgroundColor = '#f8f9ff';
                                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(79, 70, 229, 0.1)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.borderColor = '#e2e8f0';
                                    e.currentTarget.style.backgroundColor = '#fff';
                                    e.currentTarget.style.boxShadow = 'none';
                                }}
                            >
                                <div style={{ fontSize: '2rem' }}>👨‍💼</div>
                                <div>
                                    <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: '1rem' }}>
                                        Admin / HR Login
                                    </div>
                                    <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                                        Dashboard & Management
                                    </div>
                                </div>
                                <div style={{ marginLeft: 'auto', color: 'var(--primary)' }}>→</div>
                            </button>

                            {/* Employee Login Button */}
                            <button 
                                type="button"
                                onClick={() => setLoginMode('employee')}
                                style={{
                                    width: '100%',
                                    padding: '1.5rem',
                                    border: '2px solid #e2e8f0',
                                    borderRadius: '12px',
                                    backgroundColor: '#fff',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '1rem',
                                    transition: 'all 0.3s ease',
                                    textAlign: 'left'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.borderColor = 'var(--primary)';
                                    e.currentTarget.style.backgroundColor = '#f8f9ff';
                                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(79, 70, 229, 0.1)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.borderColor = '#e2e8f0';
                                    e.currentTarget.style.backgroundColor = '#fff';
                                    e.currentTarget.style.boxShadow = 'none';
                                }}
                            >
                                <div style={{ fontSize: '2rem' }}>👤</div>
                                <div>
                                    <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: '1rem' }}>
                                        Employee Login
                                    </div>
                                    <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                                        Attendance Scanning
                                    </div>
                                </div>
                                <div style={{ marginLeft: 'auto', color: 'var(--primary)' }}>→</div>
                            </button>
                        </div>
                    </>
                )}

                {/* Admin Login Form */}
                {loginMode === 'admin' && (
                    <>
                        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                            <div style={{ 
                                width: '48px', 
                                height: '48px', 
                                backgroundColor: 'var(--primary)', 
                                borderRadius: '12px', 
                                margin: '0 auto 1rem auto',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                boxShadow: '0 10px 15px -3px rgba(79, 70, 229, 0.4)',
                                fontSize: '1.2rem'
                            }}>
                                👨‍💼
                            </div>
                            <h2 style={{ margin: '0 0 0.25rem 0' }}>Admin Dashboard</h2>
                            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Login to your management dashboard</p>
                        </div>

                        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Username</label>
                                <input 
                                    type="text" 
                                    placeholder="e.g. admin" 
                                    value={username} 
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                    autoFocus
                                    style={{ width: '100%' }}
                                />
                            </div>
                            
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Password</label>
                                <input 
                                    type="password" 
                                    placeholder="••••••••" 
                                    value={password} 
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    style={{ width: '100%' }}
                                />
                            </div>

                            {error && (
                                <div style={{ 
                                    fontSize: '0.8rem', 
                                    color: 'var(--danger)', 
                                    backgroundColor: '#fee2e2', 
                                    padding: '0.75rem', 
                                    borderRadius: '8px',
                                    textAlign: 'center'
                                }}>
                                    {error}
                                </div>
                            )}

                            <button 
                                className="btn-primary" 
                                type="submit" 
                                disabled={loading}
                                style={{ width: '100%', justifyContent: 'center', padding: '0.875rem' }}
                            >
                                {loading ? 'Authenticating...' : 'Sign In'}
                            </button>
                        </form>

                        <button 
                            type="button"
                            onClick={handleBack}
                            style={{
                                marginTop: '1.25rem',
                                width: '100%',
                                padding: '0.75rem',
                                border: '1px solid #e2e8f0',
                                borderRadius: '8px',
                                backgroundColor: 'transparent',
                                cursor: 'pointer',
                                color: 'var(--text-muted)',
                                fontSize: '0.9rem',
                                transition: 'all 0.2s ease'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.backgroundColor = '#f8f9ff';
                                e.currentTarget.style.color = 'var(--primary)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.backgroundColor = 'transparent';
                                e.currentTarget.style.color = 'var(--text-muted)';
                            }}
                        >
                            ← Back to Selection
                        </button>
                    </>
                )}

                {/* Employee Login Form */}
                {loginMode === 'employee' && (
                    <>
                        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                            <div style={{ 
                                width: '48px', 
                                height: '48px', 
                                backgroundColor: 'var(--primary)', 
                                borderRadius: '12px', 
                                margin: '0 auto 1rem auto',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                boxShadow: '0 10px 15px -3px rgba(79, 70, 229, 0.4)',
                                fontSize: '1.2rem'
                            }}>
                                👤
                            </div>
                            <h2 style={{ margin: '0 0 0.25rem 0' }}>Employee Login</h2>
                            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Login for attendance scanning</p>
                        </div>

                        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Username</label>
                                <input 
                                    type="text" 
                                    placeholder="Enter your username" 
                                    value={username} 
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                    autoFocus
                                    style={{ width: '100%' }}
                                />
                            </div>
                            
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Password</label>
                                <input 
                                    type="password" 
                                    placeholder="••••••••" 
                                    value={password} 
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    style={{ width: '100%' }}
                                />
                            </div>

                            {error && (
                                <div style={{ 
                                    fontSize: '0.8rem', 
                                    color: 'var(--danger)', 
                                    backgroundColor: '#fee2e2', 
                                    padding: '0.75rem', 
                                    borderRadius: '8px',
                                    textAlign: 'center'
                                }}>
                                    {error}
                                </div>
                            )}

                            <button 
                                className="btn-primary" 
                                type="submit" 
                                disabled={loading}
                                style={{ width: '100%', justifyContent: 'center', padding: '0.875rem' }}
                            >
                                {loading ? 'Authenticating...' : 'Sign In'}
                            </button>
                        </form>

                        <button 
                            type="button"
                            onClick={handleBack}
                            style={{
                                marginTop: '1.25rem',
                                width: '100%',
                                padding: '0.75rem',
                                border: '1px solid #e2e8f0',
                                borderRadius: '8px',
                                backgroundColor: 'transparent',
                                cursor: 'pointer',
                                color: 'var(--text-muted)',
                                fontSize: '0.9rem',
                                transition: 'all 0.2s ease'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.backgroundColor = '#f8f9ff';
                                e.currentTarget.style.color = 'var(--primary)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.backgroundColor = 'transparent';
                                e.currentTarget.style.color = 'var(--text-muted)';
                            }}
                        >
                            ← Back to Selection
                        </button>
                    </>
                )}
            </div>
        </div>
    );
};

export default Login;

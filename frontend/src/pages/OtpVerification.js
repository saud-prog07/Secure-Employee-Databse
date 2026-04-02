import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const OtpVerification = () => {
    const [otp, setOtp] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const [username, setUsername] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        // Get username from localStorage that was set during login
        const pendingUsername = localStorage.getItem('pendingUsername');
        if (!pendingUsername) {
            // If no pending username, redirect to login
            navigate('/login');
            return;
        }
        setUsername(pendingUsername);
    }, [navigate]);

    const handleVerifyOtp = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        // Validate OTP format (6 digits)
        if (!/^\d{6}$/.test(otp)) {
            setError('OTP must be 6 digits');
            setLoading(false);
            return;
        }

        console.log('[OTP Verification] Verifying OTP for user:', username);

        try {
            const response = await api.post('/api/auth/verify-otp', {
                username,
                otp
            });

            console.log('[OTP Verification] Response:', response.data);

            // Validate response structure
            if (!response.data || !response.data.data) {
                console.error('[OTP Verification] Invalid response structure');
                setError('Invalid server response. Please try again.');
                return;
            }

            // Check if OTP verification was successful
            if (response.data.data.status === 'SUCCESS') {
                // Validate token exists
                if (!response.data.data.token) {
                    console.error('[OTP Verification] Token missing from response');
                    setError('Verification failed: No token received. Please try again.');
                    return;
                }

                console.log('[OTP Verification] OTP verified - storing token');
                
                // Store token, roles, and username
                localStorage.setItem('token', response.data.data.token);
                localStorage.setItem('roles', JSON.stringify(response.data.data.roles || []));
                localStorage.setItem('username', response.data.data.username || username);

                console.log('[OTP Verification] Token stored in localStorage');
                console.log('[OTP Verification] localStorage.token:', localStorage.getItem('token') ? 'Present' : 'Missing');

                // Clear pending username
                localStorage.removeItem('pendingUsername');
                
                // Get login mode and redirect accordingly
                const loginMode = localStorage.getItem('loginMode') || 'admin';
                localStorage.removeItem('loginMode');
                
                if (loginMode === 'employee') {
                    console.log('[OTP Verification] Employee mode - redirecting to attendance-scan');
                    navigate('/attendance-scan');
                } else {
                    console.log('[OTP Verification] Admin mode - redirecting to dashboard');
                    navigate('/dashboard');
                }
            } else {
                console.error('[OTP Verification] Unknown response status:', response.data.data.status);
                setError('Verification failed. Please try again.');
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || 'Invalid OTP - Please try again';
            console.error('[OTP Verification] Error:', errorMessage, err);
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    const handleBackToLogin = () => {
        localStorage.removeItem('pendingUsername');
        navigate('/login');
    };

    return (
        <div style={{
            height: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)'
        }}>
            <div className="card" style={{ width: '100%', maxWidth: '400px', padding: '2.5rem' }}>
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
                        boxShadow: '0 10px 15px -3px rgba(79, 70, 229, 0.4)'
                    }}>
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <circle cx="12" cy="12" r="1"></circle>
                            <circle cx="19" cy="12" r="1"></circle>
                            <circle cx="5" cy="12" r="1"></circle>
                        </svg>
                    </div>
                    <h2 style={{ margin: 0 }}>Two-Factor Authentication</h2>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', margin: '0.5rem 0 0 0' }}>
                        Enter the 6-digit code from your authenticator app
                    </p>
                </div>

                <form onSubmit={handleVerifyOtp} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                            Authentication Code
                        </label>
                        <input
                            type="text"
                            placeholder="000000"
                            value={otp}
                            onChange={(e) => {
                                // Only allow digits and limit to 6
                                const value = e.target.value.replace(/\D/g, '').slice(0, 6);
                                setOtp(value);
                            }}
                            maxLength="6"
                            disabled={loading}
                            required
                            style={{
                                width: '100%',
                                fontSize: '1.5rem',
                                letterSpacing: '0.5rem',
                                textAlign: 'center',
                                fontFamily: 'monospace'
                            }}
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
                        disabled={loading || otp.length !== 6}
                        style={{ width: '100%', justifyContent: 'center', padding: '0.875rem' }}
                    >
                        {loading ? 'Verifying...' : 'Verify Code'}
                    </button>
                </form>

                <div style={{
                    marginTop: '1.5rem',
                    paddingTop: '1.5rem',
                    borderTop: '1px solid #e2e8f0',
                    textAlign: 'center'
                }}>
                    <button
                        onClick={handleBackToLogin}
                        style={{
                            background: 'none',
                            border: 'none',
                            color: 'var(--primary)',
                            textDecoration: 'none',
                            fontWeight: 600,
                            fontSize: '0.95rem',
                            cursor: 'pointer',
                            padding: 0
                        }}
                    >
                        Back to Login
                    </button>
                </div>
            </div>
        </div>
    );
};

export default OtpVerification;

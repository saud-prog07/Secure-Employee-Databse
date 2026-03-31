import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            const response = await api.post('/api/auth/login', { username, password });
            localStorage.setItem('token', response.data.data.token);
            localStorage.setItem('roles', JSON.stringify(response.data.data.roles));
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Login failed - Check credentials');
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
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                        </svg>
                    </div>
                    <h2 style={{ margin: 0 }}>Secure Login</h2>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Access your management system</p>
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

                <div style={{ 
                    marginTop: '1.5rem', 
                    paddingTop: '1.5rem', 
                    borderTop: '1px solid #e2e8f0',
                    textAlign: 'center'
                }}>
                    <p style={{ margin: '0 0 0.75rem 0', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                        New user?
                    </p>
                    <Link 
                        to="/register" 
                        style={{ 
                            color: 'var(--primary)', 
                            textDecoration: 'none',
                            fontWeight: 600,
                            fontSize: '0.95rem'
                        }}
                    >
                        Create Account
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default Login;

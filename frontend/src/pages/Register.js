import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';

const Register = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        // Validation
        if (!username || !password || !confirmPassword) {
            setError('All fields are required');
            setLoading(false);
            return;
        }

        if (password !== confirmPassword) {
            setError('Passwords do not match');
            setLoading(false);
            return;
        }

        if (password.length < 5) {
            setError('Password must be at least 5 characters');
            setLoading(false);
            return;
        }

        if (username.length < 3) {
            setError('Username must be at least 3 characters');
            setLoading(false);
            return;
        }

        try {
            const response = await api.post('/api/auth/register', { 
                username, 
                password,
                role: 'HR' // Default role for self-registration
            });
            setSuccess('Registration successful! Please wait for admin approval to login.');
            setUsername('');
            setPassword('');
            setConfirmPassword('');
            
            // Redirect to login after 3 seconds
            setTimeout(() => {
                navigate('/');
            }, 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Username may already exist.');
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
            <div className="card" style={{ width: '100%', maxWidth: '450px', padding: '2.5rem' }}>
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
                            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="8.5" cy="7" r="4"></circle>
                            <path d="M20 8v6"></path>
                            <path d="M23 11h-6"></path>
                        </svg>
                    </div>
                    <h2 style={{ margin: 0 }}>Create Account</h2>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Register for the Employee Management System</p>
                </div>

                {success && (
                    <div style={{ 
                        fontSize: '0.9rem', 
                        color: 'var(--success)', 
                        backgroundColor: '#f0fdf4', 
                        padding: '1rem', 
                        borderRadius: '8px',
                        marginBottom: '1.5rem',
                        textAlign: 'center',
                        border: '1px solid #86efac'
                    }}>
                        ✓ {success}
                    </div>
                )}

                <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>USERNAME</label>
                        <input 
                            type="text" 
                            placeholder="Choose a username (min. 3 characters)" 
                            value={username} 
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            style={{ width: '100%' }}
                        />
                    </div>
                    
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>PASSWORD</label>
                        <input 
                            type="password" 
                            placeholder="Min. 5 characters" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{ width: '100%' }}
                        />
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>CONFIRM PASSWORD</label>
                        <input 
                            type="password" 
                            placeholder="Confirm your password" 
                            value={confirmPassword} 
                            onChange={(e) => setConfirmPassword(e.target.value)}
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
                            textAlign: 'center',
                            border: '1px solid #fca5a5'
                        }}>
                            ✗ {error}
                        </div>
                    )}

                    <button 
                        className="btn-primary" 
                        type="submit" 
                        disabled={loading}
                        style={{ width: '100%', justifyContent: 'center', padding: '0.875rem' }}
                    >
                        {loading ? 'Creating Account...' : 'Create Account'}
                    </button>
                </form>

                <div style={{ 
                    marginTop: '1.5rem', 
                    paddingTop: '1.5rem', 
                    borderTop: '1px solid #e2e8f0',
                    textAlign: 'center'
                }}>
                    <p style={{ margin: '0 0 0.75rem 0', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                        Already have an account?
                    </p>
                    <Link 
                        to="/" 
                        style={{ 
                            color: 'var(--primary)', 
                            textDecoration: 'none',
                            fontWeight: 600,
                            fontSize: '0.95rem'
                        }}
                    >
                        ← Back to Login
                    </Link>
                </div>

                <div style={{ 
                    marginTop: '1.5rem',
                    padding: '1rem',
                    backgroundColor: '#f8f9ff',
                    borderRadius: '8px',
                    fontSize: '0.8rem',
                    color: 'var(--text-muted)',
                    lineHeight: '1.6'
                }}>
                    <strong>Note:</strong> Your account will be created but requires admin approval before you can log in. An admin will review your request and activate your access.
                </div>
            </div>
        </div>
    );
};

export default Register;

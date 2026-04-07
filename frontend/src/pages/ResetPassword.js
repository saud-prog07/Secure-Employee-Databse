import React, { useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { FaLock } from 'react-icons/fa';
import api from '../services/api';
import './Login.css'; // Reuse high-end styles

const ResetPassword = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!token) {
            setError('Missing reset token. Please use the link sent to your email.');
            return;
        }

        if (newPassword !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setLoading(true);
        setMessage('');
        setError('');

        try {
            const response = await api.post('/api/auth/reset-password', { token, newPassword });
            setMessage(response.data.message || 'Success! Your password has been reset.');
            setTimeout(() => {
                navigate('/login');
            }, 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Error resetting password');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-bg-container">
            <div className="login-glass-card">
                <h1 className="login-title">Reset Password</h1>
                <p style={{ color: 'rgba(255, 255, 255, 0.8)', textAlign: 'center', marginBottom: '25px', fontSize: '14px' }}>
                    Enter your new password below to reset your account.
                </p>

                <form onSubmit={handleSubmit} className="login-form-content">
                    <div className="input-pill-container">
                        <input 
                            type="password" 
                            placeholder="New Password" 
                            value={newPassword} 
                            onChange={(e) => setNewPassword(e.target.value)}
                            required
                        />
                        <FaLock className="pill-icon" />
                    </div>

                    <div className="input-pill-container">
                        <input 
                            type="password" 
                            placeholder="Confirm New Password" 
                            value={confirmPassword} 
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                        />
                        <FaLock className="pill-icon" />
                    </div>

                    <button 
                        className="login-pill-button" 
                        type="submit" 
                        disabled={loading}
                    >
                        {loading ? 'Updating...' : 'Reset Password'}
                    </button>

                    <div className="register-footer">
                        <Link to="/login">Cancel and return to Login</Link>
                    </div>

                    {message && (
                        <div className="status-success-msg" style={{ backgroundColor: 'rgba(16, 185, 129, 0.2)', color: '#10b981', padding: '12px', borderRadius: '12px', marginTop: '15px', textAlign: 'center', fontSize: '13px' }}>
                            {message} Redirecting to login...
                        </div>
                    )}

                    {error && (
                        <div className="status-error-msg" style={{ backgroundColor: 'rgba(239, 68, 68, 0.2)', color: '#ef4444', padding: '12px', borderRadius: '12px', marginTop: '15px', textAlign: 'center', fontSize: '13px' }}>
                            {error}
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
};

export default ResetPassword;

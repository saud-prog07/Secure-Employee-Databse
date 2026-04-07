import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { FaEnvelope } from 'react-icons/fa';
import api from '../services/api';
import './Login.css'; // Reuse high-end styles

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage('');
        setError('');

        try {
            const response = await api.post('/api/auth/forgot-password', { email });
            setMessage(response.data.message || 'Success! If an account exists, a link has been sent.');
        } catch (err) {
            setError(err.response?.data?.message || 'Error sending request');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-bg-container">
            <div className="login-glass-card">
                <h1 className="login-title">Forgot Password</h1>
                <p style={{ color: 'rgba(255, 255, 255, 0.8)', textAlign: 'center', marginBottom: '25px', fontSize: '14px' }}>
                    Enter your email address and we'll send you a link to reset your password.
                </p>

                <form onSubmit={handleSubmit} className="login-form-content">
                    <div className="input-pill-container">
                        <input 
                            type="email" 
                            placeholder="Email Address" 
                            value={email} 
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                        <FaEnvelope className="pill-icon" />
                    </div>

                    <button 
                        className="login-pill-button" 
                        type="submit" 
                        disabled={loading}
                    >
                        {loading ? 'Sending...' : 'Send Reset Link'}
                    </button>

                    <div className="register-footer">
                        Remember your password? <Link to="/login">Back to Login</Link>
                    </div>

                    {message && (
                        <div className="status-success-msg" style={{ backgroundColor: 'rgba(16, 185, 129, 0.2)', color: '#10b981', padding: '12px', borderRadius: '12px', marginTop: '15px', textAlign: 'center', fontSize: '13px' }}>
                            {message}
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

export default ForgotPassword;

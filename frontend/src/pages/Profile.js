import React, { useState, useEffect } from 'react';
import api from '../services/api';

const Profile = () => {
    const [user, setUser] = useState(null);
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await api.get('/api/profile');
                setUser(response.data.data);
            } catch (err) {
                setError('Failed to load profile');
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    const handlePasswordUpdate = async (e) => {
        e.preventDefault();
        setSuccess(null);
        setError(null);

        if (newPassword !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (newPassword.length < 5) {
            setError('Password must be at least 5 characters');
            return;
        }

        try {
            await api.put('/api/profile/password', { newPassword });
            setSuccess('Password updated successfully');
            setNewPassword('');
            setConfirmPassword('');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to update password');
        }
    };

    if (loading) return <div style={{ padding: '4rem', textAlign: 'center' }}>Loading profile...</div>;

    return (
        <div className="profile-container">
            <header style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0 }}>My Account Profile</h1>
                <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Manage your account security and preferences.</p>
            </header>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                {/* User Info Card */}
                <div className="card">
                    <h3 style={{ marginTop: 0, marginBottom: '1.5rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>Basic Information</h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                        <div>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>USERNAME</label>
                            <div style={{ fontWeight: 600, fontSize: '1.1rem' }}>{user.username}</div>
                        </div>
                        <div>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>CURRENT ROLE</label>
                            <span className="badge" style={{ backgroundColor: '#eff6ff', color: 'var(--primary)', fontSize: '0.85rem' }}>
                                {user.role}
                            </span>
                        </div>
                        <div>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>ACCOUNT STATUS</label>
                            <span className="badge" style={{ backgroundColor: '#f0fdf4', color: 'var(--success)', fontSize: '0.85rem' }}>
                                {user.approved ? 'Verified' : 'Pending'}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Password Update Card */}
                <div className="card">
                    <h3 style={{ marginTop: 0, marginBottom: '1.5rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>Security</h3>
                    <form onSubmit={handlePasswordUpdate}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                            <div>
                                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', display: 'block', marginBottom: '0.5rem' }}>NEW PASSWORD</label>
                                <input 
                                    type="password" 
                                    placeholder="Minimum 5 characters" 
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    style={{ width: '100%' }}
                                />
                            </div>
                            <div>
                                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', display: 'block', marginBottom: '0.5rem' }}>CONFIRM PASSWORD</label>
                                <input 
                                    type="password" 
                                    placeholder="Must match new password" 
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    style={{ width: '100%' }}
                                />
                            </div>

                            {error && <div style={{ color: 'var(--danger)', fontSize: '0.85rem', textAlign: 'center' }}>{error}</div>}
                            {success && <div style={{ color: 'var(--success)', fontSize: '0.85rem', textAlign: 'center' }}>{success}</div>}

                            <button type="submit" className="btn-primary" style={{ marginTop: '0.5rem', justifyContent: 'center' }}>
                                Update Password
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Profile;

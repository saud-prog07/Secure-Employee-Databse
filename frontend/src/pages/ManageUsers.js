import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';

const ManageUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchUsers = useCallback(async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/admin/users');
            setUsers(response.data.data);
            setError(null);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch users');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const handleApprove = async (id) => {
        try {
            await api.put(`/api/admin/users/${id}/approve`);
            fetchUsers();
        } catch (err) {
            alert('Approve failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDeactivate = async (id) => {
        if (!window.confirm('Are you sure you want to deactivate this user? They will no longer be able to log in.')) return;
        try {
            await api.delete(`/api/admin/users/${id}`);
            fetchUsers();
        } catch (err) {
            alert('Deactivation failed: ' + (err.response?.data?.message || err.message));
        }
    };

    return (
        <div className="manage-users-container">
            <header style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0 }}>System User Management</h1>
                <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Approve pending HR accounts or manage administrative access.</p>
            </header>

            {error && (
                <div className="card" style={{ border: '1px solid var(--danger)', backgroundColor: '#fef2f2', color: 'var(--danger)', marginBottom: '1rem' }}>
                    {error}
                </div>
            )}

            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>Loading users...</div>
                ) : users.length === 0 ? (
                    <div style={{ padding: '4rem', textAlign: 'center' }}>No users found.</div>
                ) : (
                    <table style={{ margin: 0 }}>
                        <thead style={{ backgroundColor: '#f8fafc' }}>
                            <tr>
                                <th>Username</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'center' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id} style={{ opacity: user.deleted ? 0.6 : 1 }}>
                                    <td style={{ fontWeight: 600 }}>{user.username}</td>
                                    <td>
                                        <span className="badge" style={{ 
                                            backgroundColor: user.role === 'ADMIN' ? '#fef2f2' : '#eff6ff', 
                                            color: user.role === 'ADMIN' ? 'var(--danger)' : 'var(--primary)' 
                                        }}>
                                            {user.role}
                                        </span>
                                    </td>
                                    <td>
                                        {user.deleted ? (
                                            <span className="badge" style={{ backgroundColor: '#f1f5f9', color: '#64748b' }}>DEACTIVATED</span>
                                        ) : user.approved ? (
                                            <span className="badge" style={{ backgroundColor: '#f0fdf4', color: 'var(--success)' }}>ACTIVE</span>
                                        ) : (
                                            <span className="badge" style={{ backgroundColor: '#fff7ed', color: '#ea580c' }}>PENDING APPROVAL</span>
                                        )}
                                    </td>
                                    <td style={{ textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                                            {!user.approved && !user.deleted && (
                                                <button 
                                                    onClick={() => handleApprove(user.id)}
                                                    className="btn-primary"
                                                    style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}
                                                >
                                                    Approve
                                                </button>
                                            )}
                                            {!user.deleted && user.role !== 'ADMIN' && (
                                                <button 
                                                    onClick={() => handleDeactivate(user.id)}
                                                    style={{ 
                                                        padding: '0.25rem 0.75rem', 
                                                        fontSize: '0.75rem', 
                                                        backgroundColor: '#fee2e2', 
                                                        color: 'var(--danger)',
                                                        border: 'none',
                                                        borderRadius: '6px',
                                                        cursor: 'pointer'
                                                    }}
                                                >
                                                    Deactivate
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default ManageUsers;

import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';

const ManageUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCreateForm, setShowCreateForm] = useState(false);

    // Create User Form State
    const [newUsername, setNewUsername] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [newRole, setNewRole] = useState('HR');

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

    const handleCreateUser = async (e) => {
        e.preventDefault();
        try {
            await api.post('/api/admin/users/create', {
                username: newUsername,
                password: newPassword,
                role: newRole
            });
            setShowCreateForm(false);
            setNewUsername('');
            setNewPassword('');
            fetchUsers();
        } catch (err) {
            alert('Creation failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleApprove = async (id) => {
        try {
            await api.put(`/api/admin/users/${id}/approve`);
            fetchUsers();
        } catch (err) {
            alert('Approve failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDisapprove = async (id) => {
        try {
            await api.put(`/api/admin/users/${id}/disapprove`);
            fetchUsers();
        } catch (err) {
            alert('Disapproval failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDeactivate = async (id) => {
        if (!window.confirm('Deactivate this user? They will be unable to log in until restored.')) return;
        try {
            await api.delete(`/api/admin/users/${id}`);
            fetchUsers();
        } catch (err) {
            alert('Deactivation failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleHardDelete = async (id) => {
        if (!window.confirm('PERMANENTLY DELETE this user? This action is IRREVERSIBLE.')) return;
        try {
            await api.delete(`/api/admin/users/${id}/permanent`);
            fetchUsers();
        } catch (err) {
            alert('Hard delete failed: ' + (err.response?.data?.message || err.message));
        }
    };

    return (
        <div className="manage-users-container">
            <header style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                <div>
                    <h1 style={{ margin: 0 }}>System User Management</h1>
                    <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Manage administrative access and approve personnel applications.</p>
                </div>
                <button 
                    onClick={() => setShowCreateForm(!showCreateForm)}
                    className="btn-primary"
                >
                    {showCreateForm ? 'Cancel Creation' : 'Register New User'}
                </button>
            </header>

            {showCreateForm && (
                <div className="card" style={{ marginBottom: '2.5rem', border: '1px solid var(--primary-light)' }}>
                    <h3 style={{ marginTop: 0 }}>Direct User Registration</h3>
                    <form onSubmit={handleCreateUser} style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem', alignItems: 'flex-end' }}>
                        <div>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>USERNAME</label>
                            <input 
                                type="text" 
                                value={newUsername} 
                                onChange={(e) => setNewUsername(e.target.value)} 
                                required 
                                style={{ width: '100%' }}
                            />
                        </div>
                        <div>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>PASSWORD</label>
                            <input 
                                type="password" 
                                value={newPassword} 
                                onChange={(e) => setNewPassword(e.target.value)} 
                                required 
                                style={{ width: '100%' }}
                            />
                        </div>
                        <div>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>ROLE</label>
                            <select value={newRole} onChange={(e) => setNewRole(e.target.value)} style={{ width: '100%' }}>
                                <option value="HR">HR Officer</option>
                                <option value="ADMIN">System Admin</option>
                            </select>
                        </div>
                        <button type="submit" className="btn-primary" style={{ width: '100%', justifyContent: 'center' }}>Create User</button>
                    </form>
                </div>
            )}

            {error && (
                <div className="card" style={{ border: '1px solid var(--danger)', backgroundColor: '#fef2f2', color: 'var(--danger)', marginBottom: '1rem' }}>
                    {error}
                </div>
            )}

            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>Synching user records...</div>
                ) : users.length === 0 ? (
                    <div style={{ padding: '4rem', textAlign: 'center' }}>Project database contains no user records.</div>
                ) : (
                    <table style={{ margin: 0 }}>
                        <thead style={{ backgroundColor: '#f8fafc' }}>
                            <tr>
                                <th>Account Identity</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'center' }}>Governance Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id} style={{ opacity: user.deleted ? 0.5 : 1 }}>
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
                                            <span className="badge" style={{ backgroundColor: '#64748b', color: 'white' }}>TERMINATED</span>
                                        ) : user.approved ? (
                                            <span className="badge" style={{ backgroundColor: '#f0fdf4', color: 'var(--success)' }}>AUTHORIZED</span>
                                        ) : (
                                            <span className="badge" style={{ backgroundColor: '#fff7ed', color: '#ea580c' }}>PENDING REVIEW</span>
                                        )}
                                    </td>
                                    <td style={{ textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                                            {!user.approved && !user.deleted && (
                                                <button onClick={() => handleApprove(user.id)} className="btn-primary" style={{ padding: '0.2rem 0.6rem', fontSize: '0.7rem' }}>Approve</button>
                                            )}
                                            {user.approved && user.role !== 'ADMIN' && (
                                                <button onClick={() => handleDisapprove(user.id)} style={{ padding: '0.2rem 0.6rem', fontSize: '0.7rem', backgroundColor: '#fbbf24', color: '#92400e', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Disapprove</button>
                                            )}
                                            {!user.deleted && user.role !== 'ADMIN' && (
                                                <button 
                                                    onClick={() => handleDeactivate(user.id)}
                                                    style={{ padding: '0.2rem 0.6rem', fontSize: '0.7rem', backgroundColor: '#fee2e2', color: 'var(--danger)', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                                >
                                                    Deactivate
                                                </button>
                                            )}
                                            {user.role !== 'ADMIN' && (
                                                <button 
                                                    onClick={() => handleHardDelete(user.id)}
                                                    style={{ padding: '0.2rem 0.6rem', fontSize: '0.7rem', backgroundColor: '#000', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                                >
                                                    Hard Delete
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

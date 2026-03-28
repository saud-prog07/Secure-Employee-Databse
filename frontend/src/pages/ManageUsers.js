import React, { useEffect, useState } from 'react';
import api from '../services/api';

const ManageUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/admin/users');
            setUsers(response.data.data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch users');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleApprove = async (id) => {
        try {
            await api.put(`/api/admin/users/${id}/approve`);
            fetchUsers();
        } catch (err) {
            alert('Approval failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to deactivate this user?')) {
            try {
                await api.delete(`/api/admin/users/${id}`);
                fetchUsers();
            } catch (err) {
                alert('Deactivation failed: ' + (err.response?.data?.message || err.message));
            }
        }
    };

    return (
        <div>
            <h2>Manage System Users (Admin Only)</h2>
            {loading && <p>Loading users...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
            
            {!loading && !error && (
                <table border="1" style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse', marginTop: '20px' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#f2f2f2' }}>
                            <th style={{ padding: '10px' }}>Username</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Deactivated</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map(user => (
                            <tr key={user.id}>
                                <td style={{ padding: '10px' }}>{user.username}</td>
                                <td>{user.role}</td>
                                <td style={{ color: user.approved ? 'green' : 'orange', fontWeight: 'bold' }}>
                                    {user.approved ? 'Approved' : 'Pending'}
                                </td>
                                <td>{user.deleted ? 'Yes' : 'No'}</td>
                                <td>
                                    {!user.approved && (
                                        <button onClick={() => handleApprove(user.id)} style={{ backgroundColor: 'green', color: 'white' }}>Approve</button>
                                    )}
                                    {!user.deleted && (
                                        <button onClick={() => handleDelete(user.id)} style={{ backgroundColor: 'red', color: 'white', marginLeft: '5px' }}>Deactivate</button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default ManageUsers;

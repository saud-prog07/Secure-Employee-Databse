import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Dashboard = () => {
    const [employees, setEmployees] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchName, setSearchName] = useState('');
    const [filterDept, setFilterDept] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [includeDeleted, setIncludeDeleted] = useState(false);
    const navigate = useNavigate();

    const fetchEmployees = useCallback(async () => {
        try {
            setLoading(true);
            const params = {
                includeDeleted
            };
            if (searchName) params.name = searchName;
            if (filterDept) params.department = filterDept;
            if (filterStatus) params.status = filterStatus;

            const response = await api.get('/api/v1/employees/search', { params });
            setEmployees(response.data.data.content);
            setError(null);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch employees.');
        } finally {
            setLoading(false);
        }
    }, [searchName, filterDept, filterStatus, includeDeleted]);

    useEffect(() => {
        fetchEmployees();
    }, [fetchEmployees]);

    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    const isAdmin = roles.includes('ROLE_ADMIN');

    const handleApprove = async (id) => {
        try {
            await api.put(`/api/v1/employees/${id}/approve`);
            fetchEmployees();
        } catch (err) {
            alert('Approval failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleRestore = async (id) => {
        try {
            await api.put(`/api/v1/employees/${id}/restore`);
            fetchEmployees();
        } catch (err) {
            alert('Restore failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this employee? (Soft-delete)')) {
            try {
                await api.delete(`/api/v1/employees/${id}`);
                fetchEmployees();
            } catch (err) {
                alert('Delete failed: ' + (err.response?.data?.message || err.message));
            }
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('roles');
        navigate('/');
    };

    return (
        <div className="dashboard-container">
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <div>
                    <h1 style={{ margin: 0, fontSize: '1.875rem' }}>Employee Management</h1>
                    <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Manage organizational records and approvals</p>
                </div>
                <div style={{ display: 'flex', gap: '0.75rem' }}>
                    <button className="btn-primary" onClick={() => navigate('/add')}>
                        <span style={{ fontSize: '1.2rem' }}>+</span> Add Employee
                    </button>
                    <button onClick={logout} style={{ backgroundColor: '#f1f5f9', color: '#475569' }}>
                        Logout
                    </button>
                </div>
            </header>

            <div className="card" style={{ marginBottom: '2rem' }}>
                <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Search Name</label>
                        <input 
                            type="text" 
                            placeholder="e.g. John Doe" 
                            value={searchName} 
                            onChange={(e) => setSearchName(e.target.value)}
                        />
                    </div>
                    
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Department</label>
                        <select value={filterDept} onChange={(e) => setFilterDept(e.target.value)}>
                            <option value="">All Departments</option>
                            <option value="IT">IT</option>
                            <option value="HR">HR</option>
                            <option value="Engineering">Engineering</option>
                            <option value="Sales">Sales</option>
                        </select>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status</label>
                        <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                            <option value="">All Statuses</option>
                            <option value="PENDING">Pending</option>
                            <option value="APPROVED">Approved</option>
                        </select>
                    </div>

                    {isAdmin && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                            <input 
                                type="checkbox" 
                                id="includeDeleted"
                                checked={includeDeleted} 
                                onChange={(e) => setIncludeDeleted(e.target.checked)}
                                style={{ minWidth: 'auto', width: '1.2rem', height: '1.2rem' }}
                            />
                            <label htmlFor="includeDeleted" style={{ fontSize: '0.875rem', fontWeight: 500 }}>Show Deleted</label>
                        </div>
                    )}

                    <button className="btn-primary" onClick={fetchEmployees} style={{ height: '42px' }}>
                        Apply Filters
                    </button>
                </div>
            </div>

            {error && (
                <div className="card" style={{ border: '1px solid var(--danger)', backgroundColor: '#fef2f2', color: 'var(--danger)', marginBottom: '1rem' }}>
                    {error}
                </div>
            )}

            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading employee data...
                    </div>
                ) : employees.length === 0 ? (
                    <div style={{ padding: '4rem', textAlign: 'center' }}>
                        <h3 style={{ margin: 0 }}>No employees match your criteria</h3>
                        <p style={{ color: 'var(--text-muted)' }}>Try adjusting your filters or search terms.</p>
                    </div>
                ) : (
                    <table style={{ margin: 0 }}>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Department</th>
                                <th>Salary</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {employees.map(emp => (
                                <tr key={emp.id} style={{ opacity: emp.deleted ? 0.6 : 1 }}>
                                    <td>
                                        <div style={{ fontWeight: 600 }}>{emp.name}</div>
                                        {emp.deleted && <span className="badge badge-deleted" style={{ fontSize: '0.6rem', marginTop: '0.2rem' }}>SOFT DELETED</span>}
                                    </td>
                                    <td>{emp.email}</td>
                                    <td>{emp.department}</td>
                                    <td style={{ fontWeight: 500 }}>${emp.salary?.toLocaleString()}</td>
                                    <td>
                                        <span className={`badge badge-${emp.status.toLowerCase()}`}>
                                            {emp.status}
                                        </span>
                                    </td>
                                    <td style={{ textAlign: 'right' }}>
                                        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                                            {isAdmin && emp.deleted ? (
                                                <button className="btn-success" onClick={() => handleRestore(emp.id)} title="Restore Employee">
                                                    Restore
                                                </button>
                                            ) : (
                                                <>
                                                    {isAdmin && emp.status === 'PENDING' && (
                                                        <button className="btn-success" onClick={() => handleApprove(emp.id)}>
                                                            Approve
                                                        </button>
                                                    )}
                                                    {isAdmin && (
                                                        <button className="btn-danger" onClick={() => handleDelete(emp.id)}>
                                                            Delete
                                                        </button>
                                                    )}
                                                </>
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

export default Dashboard;

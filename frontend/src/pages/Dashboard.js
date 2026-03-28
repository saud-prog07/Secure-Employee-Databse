import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Dashboard = () => {
    const [employees, setEmployees] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchName, setSearchName] = useState('');
    const [filterDept, setFilterDept] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const navigate = useNavigate();

    const fetchEmployees = async () => {
        try {
            setLoading(true);
            const params = {};
            if (searchName) params.name = searchName;
            if (filterDept) params.department = filterDept;
            if (filterStatus) params.status = filterStatus;

            // Use the combined search endpoint for all filtered requests
            const response = await api.get('/api/v1/employees/search', { params });
            setEmployees(response.data.data.content);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch employees.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchEmployees();
    }, []);

    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    const isAdmin = roles.includes('ROLE_ADMIN');

    const handleApprove = async (id) => {
        try {
            await api.put(`/api/v1/employees/${id}/approve`);
            fetchEmployees(); // Refresh the list after approval
        } catch (err) {
            alert('Approval failed: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this employee? (Soft-delete)')) {
            try {
                await api.delete(`/api/v1/employees/${id}`);
                fetchEmployees(); // Refresh list
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
        <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h2 style={{ margin: 0 }}>Dashboard - Employee List</h2>
            <div style={{ display: 'flex', gap: '10px' }}>
                <button 
                    onClick={() => navigate('/add')}
                    style={{ padding: '8px 15px', backgroundColor: '#2ecc71', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                >
                    + Add New Employee
                </button>
                <button 
                    onClick={logout}
                    style={{ padding: '8px 15px', backgroundColor: '#e74c3c', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                    Logout
                </button>
            </div>
        </div>
        <div style={{ padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '8px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
                <input 
                    type="text" 
                    placeholder="Search by Name..." 
                    value={searchName} 
                    onChange={(e) => setSearchName(e.target.value)}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                />
                <select 
                    value={filterDept} 
                    onChange={(e) => setFilterDept(e.target.value)}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                >
                    <option value="">All Departments</option>
                    <option value="IT">IT</option>
                    <option value="HR">HR</option>
                    <option value="Engineering">Engineering</option>
                    <option value="Sales">Sales</option>
                </select>
                <select 
                    value={filterStatus} 
                    onChange={(e) => setFilterStatus(e.target.value)}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                >
                    <option value="">All Statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Approved</option>
                </select>
                <button 
                    onClick={fetchEmployees}
                    style={{ padding: '8px 15px', backgroundColor: '#3498db', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                    Search
                </button>
                <button 
                    onClick={() => { setSearchName(''); setFilterDept(''); setFilterStatus(''); fetchEmployees(); }}
                    style={{ padding: '8px 15px', backgroundColor: '#95a5a6', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                    Clear Filters
                </button>
            </div>

            {loading && <div style={{ padding: '20px', textAlign: 'center' }}>Loading employees...</div>}
            
            {!loading && error && <div style={{ color: 'red', padding: '10px', border: '1px solid red', marginBottom: '10px' }}>{error}</div>}
            
            {!loading && !error && (
                <>
                    {employees.length === 0 ? (
                        <div style={{ padding: '40px', textAlign: 'center', border: '1px dashed #ccc', marginTop: '20px' }}>
                            <h3>No employees found</h3>
                            <p>Try adding a new record or checking your connectivity.</p>
                        </div>
                    ) : (
                        <table border="1" style={{ marginTop: '20px', width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ backgroundColor: '#f2f2f2' }}>
                                    <th style={{ padding: '10px' }}>Name</th>
                                    <th>Email</th>
                                    <th>Department</th>
                                    <th>Salary</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {employees.map(emp => (
                                    <tr key={emp.id}>
                                        <td style={{ padding: '10px' }}>{emp.name}</td>
                                        <td>{emp.email}</td>
                                        <td>{emp.department}</td>
                                        <td>${emp.salary?.toLocaleString() || '0'}</td>
                                        <td style={{ 
                                            fontWeight: 'bold', 
                                            color: emp.status === 'PENDING' ? '#f39c12' : '#27ae60' 
                                        }}>
                                            {emp.status}
                                        </td>
                                        <td style={{ display: 'flex', gap: '5px' }}>
                                            {isAdmin && emp.status === 'PENDING' && (
                                                <button 
                                                    onClick={() => handleApprove(emp.id)}
                                                    style={{ cursor: 'pointer', backgroundColor: '#27ae60', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px' }}
                                                >
                                                    Approve
                                                </button>
                                            )}
                                            {isAdmin && (
                                                <button 
                                                    onClick={() => handleDelete(emp.id)}
                                                    style={{ cursor: 'pointer', backgroundColor: '#e74c3c', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px' }}
                                                >
                                                    Delete
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </>
            )}
        </div>
    );
};

export default Dashboard;

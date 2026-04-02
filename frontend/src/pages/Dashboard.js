import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import QRCodeDisplay from '../components/QRCodeDisplay';

const Dashboard = () => {
    const [employees, setEmployees] = useState([]);
    const [pendingEmployees, setPendingEmployees] = useState([]);
    const [pendingCount, setPendingCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [searchName, setSearchName] = useState('');
    const [filterDept, setFilterDept] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [includeDeleted, setIncludeDeleted] = useState(false);
    const [showPendingOnly, setShowPendingOnly] = useState(false);
    const [selectedQREmployee, setSelectedQREmployee] = useState(null);
    const navigate = useNavigate();

    const token = localStorage.getItem('token');
    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    const isAdmin = roles.includes('ROLE_ADMIN');

    console.log('[Dashboard] Mounted', { hasToken: !!token, roles, isAdmin });

    // Verify token is valid on component mount
    useEffect(() => {
        if (!token) {
            console.error('[Dashboard] No token found - redirecting to login');
            navigate('/', { replace: true });
            return;
        }
        
        console.log('[Dashboard] Token verified - proceeding with data fetch');
    }, [token, navigate]);

    // Auto-fetch pending count for admin
    useEffect(() => {
        if (isAdmin) {
            const fetchPendingCount = async () => {
                try {
                    const response = await api.get('/api/v1/employees/approvals/pending/count');
                    if (response.data && response.data.data && response.data.data.pendingCount !== undefined) {
                        setPendingCount(response.data.data.pendingCount);
                    }
                } catch (err) {
                    console.error('Failed to fetch pending count:', err);
                    setPendingCount(0);
                }
            };
            
            fetchPendingCount();
            const interval = setInterval(fetchPendingCount, 10000); // Refresh every 10 seconds
            return () => clearInterval(interval);
        }
    }, [isAdmin]);

    const fetchPendingEmployees = useCallback(async () => {
        if (!isAdmin) return;
        try {
            const response = await api.get('/api/v1/employees/approvals/pending', { params: { size: 5 } });
            if (response.data && response.data.data) {
                setPendingEmployees(Array.isArray(response.data.data.content) ? response.data.data.content : []);
            }
        } catch (err) {
            console.error('Failed to fetch pending approvals:', err);
            setPendingEmployees([]);
            // Don't show error for pending fetch - it's secondary
        }
    }, [isAdmin]);

    const fetchEmployees = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            
            const params = {
                includeDeleted,
                page: 0,
                size: 100  // Fetch up to 100 employees
            };
            if (searchName) params.name = searchName;
            if (filterDept) params.department = filterDept;
            if (filterStatus) params.status = filterStatus;

            console.log('[Dashboard] Fetching employees with params:', params);
            
            const response = await api.get('/api/v1/employees/search', { params });
            
            console.log('[Dashboard] Employees response:', response.data);
            
            // Validate response structure
            if (!response.data) {
                console.error('[Dashboard] Invalid response:', response.data);
                setError('Invalid server response. Please refresh the page.');
                setEmployees([]);
                return;
            }
            
            // Handle different response structures
            let employees = [];
            if (response.data.data && response.data.data.content) {
                employees = response.data.data.content;
            } else if (response.data.data && Array.isArray(response.data.data)) {
                employees = response.data.data;
            } else if (Array.isArray(response.data)) {
                employees = response.data;
            }
            
            console.log('[Dashboard] Processed employees:', employees);
            setEmployees(Array.isArray(employees) ? employees : []);
            
        } catch (err) {
            console.error('Error fetching employees:', err);
            
            // Handle different error scenarios
            let errorMessage = 'Failed to load employees.';
            
            if (err.response) {
                // Server responded with error status
                if (err.response.status === 401) {
                    errorMessage = 'Your session has expired. Please login again.';
                    localStorage.removeItem('token');
                    localStorage.removeItem('roles');
                    navigate('/');
                    return;
                } else if (err.response.status === 403) {
                    errorMessage = 'You do not have permission to view employees.';
                } else if (err.response.status === 500) {
                    errorMessage = 'Server error. Please try again later.';
                } else {
                    errorMessage = err.response.data?.message || 'Failed to fetch employees.';
                }
            } else if (err.request) {
                // Request made but no response
                errorMessage = 'No response from server. Please check your connection.';
            } else {
                // Error in request setup
                errorMessage = err.message || 'An error occurred while loading employees.';
            }
            
            setError(errorMessage);
            setEmployees([]);
        } finally {
            setLoading(false);
        }
    }, [searchName, filterDept, filterStatus, includeDeleted, navigate]);

    useEffect(() => {
        fetchEmployees();
        fetchPendingEmployees();
    }, [fetchEmployees, fetchPendingEmployees]);

    const handleApprove = async (id) => {
        try {
            setSuccess(null);
            await api.put(`/api/v1/employees/${id}/approve`);
            setSuccess('✓ Employee approved successfully! Notification email sent.');
            fetchEmployees();
            fetchPendingEmployees();
            setTimeout(() => setSuccess(null), 5000);
        } catch (err) {
            setError('Approval failed: ' + (err.response?.data?.message || err.message));
            setTimeout(() => setError(null), 5000);
        }
    };

    const handleReject = async (id) => {
        if (!window.confirm('Are you sure you want to reject this employee?')) return;
        try {
            setSuccess(null);
            await api.put(`/api/v1/employees/${id}/reject`);
            setSuccess('✓ Employee rejected successfully! Notification email sent.');
            fetchEmployees();
            fetchPendingEmployees();
            setTimeout(() => setSuccess(null), 5000);
        } catch (err) {
            setError('Rejection failed: ' + (err.response?.data?.message || err.message));
            setTimeout(() => setError(null), 5000);
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

    const getStatusColor = (status) => {
        switch(status) {
            case 'PENDING':
                return { bg: '#fef3c7', color: '#b45309' };
            case 'APPROVED':
                return { bg: '#dcfce7', color: '#166534' };
            case 'REJECTED':
                return { bg: '#fee2e2', color: '#991b1b' };
            default:
                return { bg: '#f1f5f9', color: '#475569' };
        }
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
                    <button onClick={logout} style={{ backgroundColor: '#f1f5f9', color: '#475569', fontWeight: 600 }}>
                        Logout
                    </button>
                </div>
            </header>

            {isAdmin && pendingCount > 0 && (
                <div className="card" style={{ backgroundColor: '#fef3c7', border: '1px solid #fcd34d', marginBottom: '2rem', padding: '1.5rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <h2 style={{ margin: 0, color: '#b45309', fontSize: '1.125rem', fontWeight: 700 }}>
                                ⚠️ Pending Approvals ({pendingCount})
                            </h2>
                            <p style={{ margin: '0.5rem 0 0 0', color: '#92400e', fontSize: '0.875rem' }}>
                                {pendingCount} employee{pendingCount !== 1 ? 's' : ''} awaiting your approval
                            </p>
                        </div>
                        <button 
                            className="btn-primary"
                            onClick={() => setShowPendingOnly(true)}
                            style={{ backgroundColor: '#b45309' }}
                        >
                            View All
                        </button>
                    </div>
                    
                    {pendingEmployees.length > 0 && (
                        <div style={{ marginTop: '1rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1rem' }}>
                            {pendingEmployees.slice(0, 5).map(emp => (
                                <div key={emp.id} style={{ backgroundColor: 'white', padding: '1rem', borderRadius: '4px', border: '1px solid #fde68a' }}>
                                    <h3 style={{ margin: '0 0 0.5rem 0', fontWeight: 600, color: '#1f2937' }}>{emp.name}</h3>
                                    <p style={{ margin: '0.25rem 0', fontSize: '0.875rem', color: '#6b7280' }}>{emp.email}</p>
                                    <p style={{ margin: '0.25rem 0', fontSize: '0.875rem', color: '#6b7280' }}>{emp.department}</p>
                                    <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
                                        <button 
                                            className="btn-success"
                                            onClick={() => handleApprove(emp.id)}
                                            style={{ flex: 1, fontSize: '0.875rem', padding: '0.5rem' }}
                                        >
                                            Approve
                                        </button>
                                        <button 
                                            onClick={() => handleReject(emp.id)}
                                            style={{ 
                                                flex: 1, 
                                                backgroundColor: '#fee2e2', 
                                                color: '#991b1b', 
                                                border: 'none', 
                                                borderRadius: '4px', 
                                                padding: '0.5rem', 
                                                fontWeight: 600, 
                                                cursor: 'pointer',
                                                fontSize: '0.875rem'
                                            }}
                                        >
                                            Reject
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

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
                            <option value="Marketing">Marketing</option>
                            <option value="Sales">Sales</option>
                            <option value="Finance">Finance</option>
                        </select>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status</label>
                        <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                            <option value="">All Statuses</option>
                            <option value="PENDING">Pending</option>
                            <option value="APPROVED">Approved</option>
                            <option value="REJECTED">Rejected</option>
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
                <div className="card" style={{ 
                    border: '1px solid #dc2626', 
                    backgroundColor: '#fef2f2', 
                    color: '#991b1b', 
                    marginBottom: '1rem',
                    padding: '1rem',
                    borderRadius: '4px'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                        <div>
                            <strong>Error:</strong> {error}
                        </div>
                        <button 
                            onClick={() => setError(null)}
                            style={{
                                backgroundColor: 'transparent',
                                border: 'none',
                                color: '#991b1b',
                                cursor: 'pointer',
                                fontSize: '1.25rem',
                                padding: '0'
                            }}
                        >
                            ×
                        </button>
                    </div>
                </div>
            )}

            {success && (
                <div className="card" style={{ 
                    border: '1px solid #16a34a', 
                    backgroundColor: '#f0fdf4', 
                    color: '#166534', 
                    marginBottom: '1rem',
                    padding: '1rem',
                    borderRadius: '4px'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                        <div>{success}</div>
                        <button 
                            onClick={() => setSuccess(null)}
                            style={{
                                backgroundColor: 'transparent',
                                border: 'none',
                                color: '#166534',
                                cursor: 'pointer',
                                fontSize: '1.25rem',
                                padding: '0'
                            }}
                        >
                            ×
                        </button>
                    </div>
                </div>
            )}

            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        <div style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Loading...</div>
                        <p>Fetching employee data from server...</p>
                    </div>
                ) : employees.length === 0 ? (
                    <div style={{ padding: '4rem', textAlign: 'center' }}>
                        <div style={{ fontSize: '3rem', marginBottom: '1rem', color: '#d1d5db' }}>∅</div>
                        <h3 style={{ margin: '0 0 0.5rem 0', color: '#6b7280' }}>No Records Found</h3>
                        <p style={{ color: 'var(--text-muted)', margin: 0 }}>
                            No employees match your current filter criteria.
                        </p>
                        <div style={{ marginTop: '1.5rem', display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                            <button 
                                className="btn-primary"
                                onClick={() => {
                                    setSearchName('');
                                    setFilterDept('');
                                    setFilterStatus('');
                                    setIncludeDeleted(false);
                                }}
                            >
                                Clear Filters
                            </button>
                            <button 
                                className="btn-primary"
                                onClick={() => navigate('/add')}
                            >
                                Add New Employee
                            </button>
                        </div>
                    </div>
                ) : (
                    <table style={{ margin: 0 }}>
                        <thead style={{ backgroundColor: '#f8fafc' }}>
                            <tr>
                                <th>Name</th>
                                <th>Identity</th>
                                <th>Org Dept</th>
                                <th>Salary</th>
                                <th>Review Status</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {employees.map(emp => {
                                const statusColor = getStatusColor(emp.status);
                                return (
                                <tr key={emp.id} style={{ opacity: emp.deleted ? 0.6 : 1 }}>
                                    <td>
                                        <div style={{ fontWeight: 600 }}>{emp.name}</div>
                                        {emp.deleted && <span className="badge" style={{ backgroundColor: '#64748b', color: 'white', fontSize: '0.6rem' }}>SOFT DELETED</span>}
                                    </td>
                                    <td>{emp.email}</td>
                                    <td>{emp.department}</td>
                                    <td style={{ fontWeight: 500 }}>${emp.salary?.toLocaleString()}</td>
                                    <td>
                                        <span 
                                            className="badge"
                                            style={{ 
                                                backgroundColor: statusColor.bg, 
                                                color: statusColor.color,
                                                fontWeight: 600,
                                                padding: '0.375rem 0.75rem'
                                            }}
                                        >
                                            {emp.status}
                                        </span>
                                        {emp.approvedAt && (
                                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                                                by {emp.approvedBy} on {new Date(emp.approvedAt).toLocaleDateString()}
                                            </div>
                                        )}
                                    </td>
                                    <td style={{ textAlign: 'right' }}>
                                        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                                            {isAdmin && emp.deleted ? (
                                                <button className="btn-success" onClick={() => handleRestore(emp.id)}>
                                                    Restore
                                                </button>
                                            ) : (
                                                <>
                                                    <button 
                                                        onClick={() => navigate(`/edit/${emp.id}`)}
                                                        style={{ backgroundColor: '#f1f5f9', color: '#475569', border: 'none', borderRadius: '4px', padding: '0.5rem 1rem', fontWeight: 600, cursor: 'pointer', fontSize: '0.875rem' }}
                                                    >
                                                        Edit
                                                    </button>
                                                    <button 
                                                        onClick={() => setSelectedQREmployee(emp)}
                                                        style={{ backgroundColor: '#dbeafe', color: '#0369a1', border: 'none', borderRadius: '4px', padding: '0.5rem 1rem', fontWeight: 600, cursor: 'pointer', fontSize: '0.875rem' }}
                                                    >
                                                        QR Code
                                                    </button>
                                                    {isAdmin && emp.status === 'PENDING' && (
                                                        <button className="btn-success" onClick={() => handleApprove(emp.id)}>
                                                            Approve
                                                        </button>
                                                    )}
                                                    {isAdmin && (emp.status === 'APPROVED' || emp.status === 'REJECTED') && (
                                                        <button 
                                                            onClick={() => handleReject(emp.id)}
                                                            style={{ backgroundColor: '#fee2e2', color: '#991b1b', border: 'none', borderRadius: '4px', padding: '0.5rem 1rem', fontWeight: 600, cursor: 'pointer', fontSize: '0.875rem' }}
                                                        >
                                                            {emp.status === 'APPROVED' ? 'Reject' : 'Revert'}
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
                            )})}
                        </tbody>
                    </table>
                )}
            </div>

            {selectedQREmployee && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    zIndex: 1000
                }}>
                    <div className="card" style={{ maxWidth: '400px', width: '90%', position: 'relative' }}>
                        <button 
                            onClick={() => setSelectedQREmployee(null)}
                            style={{
                                position: 'absolute',
                                top: '1rem',
                                right: '1rem',
                                border: 'none',
                                backgroundColor: '#f1f5f9',
                                borderRadius: '4px',
                                padding: '0.5rem 1rem',
                                cursor: 'pointer',
                                fontWeight: 600
                            }}
                        >
                            Close
                        </button>
                        <div style={{ textAlign: 'center', paddingTop: '2rem' }}>
                            <h3 style={{ margin: '0 0 1rem 0' }}>QR Code</h3>
                            <div style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
                                {selectedQREmployee.name} (ID: {selectedQREmployee.id})
                            </div>
                            <QRCodeDisplay employeeId={selectedQREmployee.id} />
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Dashboard;

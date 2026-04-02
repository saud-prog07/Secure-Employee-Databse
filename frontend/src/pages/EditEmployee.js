import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../services/api';

const EditEmployee = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [department, setDepartment] = useState('');
    const [salary, setSalary] = useState('');
    const [status, setStatus] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(true);
    const [attendanceSummary, setAttendanceSummary] = useState(null);
    const [attendanceLoading, setAttendanceLoading] = useState(false);

    useEffect(() => {
        const fetchEmployee = async () => {
            try {
                const response = await api.get(`/api/v1/employees/${id}`);
                const emp = response.data.data;
                setName(emp.name);
                setEmail(emp.email);
                setDepartment(emp.department);
                setSalary(emp.salary);
                setStatus(emp.status);
            } catch (err) {
                setError('Failed to load employee record.');
            } finally {
                setLoading(false);
            }
        };
        const fetchAttendanceSummary = async () => {
            setAttendanceLoading(true);
            try {
                const response = await api.get(`/api/attendance/summary/${id}`);
                setAttendanceSummary(response.data.data);
            } catch (err) {
                console.log('Attendance summary not available');
            } finally {
                setAttendanceLoading(false);
            }
        };
        fetchEmployee();
        fetchAttendanceSummary();
    }, [id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            await api.put(`/api/v1/employees/${id}`, { 
                name, 
                email, 
                department, 
                salary: parseFloat(salary),
                status
            });
            setSuccess('Professional record updated successfully.');
            setTimeout(() => navigate('/dashboard'), 1500);
        } catch (err) {
            setError(err.response?.data?.message || 'Update operation failed.');
        }
    };

    if (loading) return <div className="container" style={{ textAlign: 'center', padding: '5rem' }}>Hydrating record data...</div>;

    return (
        <div className="add-employee-container" style={{ maxWidth: '600px', margin: '0 auto' }}>
            <header style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0 }}>Sync Employee Record</h1>
                <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Update professional credentials for record ID: #{id}</p>
            </header>

            <div className="card">
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>FULL NAME</label>
                            <input type="text" value={name} onChange={(e) => setName(e.target.value)} required />
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>EMAIL IDENTITY</label>
                            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>DEPARTMENT</label>
                            <select value={department} onChange={(e) => setDepartment(e.target.value)} required>
                                <option value="Engineering">Engineering</option>
                                <option value="Marketing">Marketing</option>
                                <option value="HR">HR</option>
                                <option value="Sales">Sales</option>
                                <option value="Finance">Finance</option>
                                <option value="Operations">Operations</option>
                                <option value="IT">IT</option>
                            </select>
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>ANNUAL SALARY ($)</label>
                            <input type="number" value={salary} onChange={(e) => setSalary(e.target.value)} required />
                        </div>
                    </div>

                    {error && <div style={{ color: 'var(--danger)', fontSize: '0.85rem', backgroundColor: '#fef2f2', padding: '0.75rem', borderRadius: '8px', textAlign: 'center' }}>{error}</div>}
                    {success && <div style={{ color: 'var(--success)', fontSize: '0.85rem', backgroundColor: '#f0fdf4', padding: '0.75rem', borderRadius: '8px', textAlign: 'center' }}>{success}</div>}

                    <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                        <button type="submit" className="btn-primary" style={{ flex: 1, justifyContent: 'center' }}>Commit Changes</button>
                        <button 
                            type="button" 
                            onClick={() => navigate('/dashboard')}
                            style={{ 
                                flex: 1, 
                                backgroundColor: 'white', 
                                border: '1px solid #e2e8f0', 
                                color: 'var(--text-muted)',
                                borderRadius: '8px',
                                fontWeight: 600,
                                cursor: 'pointer'
                            }}
                        >
                            Abort
                        </button>
                    </div>
                </form>
            </div>

            {attendanceSummary && (
                <div className="card" style={{ marginTop: '2rem' }}>
                    <header style={{ marginBottom: '1.5rem' }}>
                        <h2 style={{ margin: 0 }}>Attendance Analytics</h2>
                        <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Performance metrics and attendance record</p>
                    </header>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                        <div style={{
                            backgroundColor: '#f8fafc',
                            padding: '1.5rem',
                            borderRadius: '8px',
                            textAlign: 'center',
                            border: '2px solid #e2e8f0'
                        }}>
                            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#2563eb', marginBottom: '0.5rem' }}>
                                {attendanceSummary.attendancePercentage}%
                            </div>
                            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Attendance Rate</div>
                            <div style={{
                                marginTop: '0.75rem',
                                display: 'inline-block',
                                padding: '0.35rem 0.75rem',
                                borderRadius: '20px',
                                fontSize: '0.75rem',
                                fontWeight: 600,
                                backgroundColor: attendanceSummary.status === 'GOOD' ? '#dcfce7' : attendanceSummary.status === 'AVERAGE' ? '#fef3c7' : '#fee2e2',
                                color: attendanceSummary.status === 'GOOD' ? '#166534' : attendanceSummary.status === 'AVERAGE' ? '#92400e' : '#991b1b'
                            }}>
                                {attendanceSummary.status} ({attendanceSummary.attendancePercentage >= 75 ? 'Excellent' : attendanceSummary.attendancePercentage >= 60 ? 'Satisfactory' : 'Requires Attention'})
                            </div>
                        </div>

                        <div style={{
                            backgroundColor: '#f8fafc',
                            padding: '1.5rem',
                            borderRadius: '8px',
                            border: '2px solid #e2e8f0'
                        }}>
                            <div style={{
                                display: 'grid',
                                gridTemplateColumns: '1fr 1fr',
                                gap: '1rem',
                                marginBottom: '1rem'
                            }}>
                                <div style={{ textAlign: 'center' }}>
                                    <div style={{ fontSize: '1.75rem', fontWeight: 'bold', color: '#16a34a', marginBottom: '0.25rem' }}>
                                        {attendanceSummary.presentDays}
                                    </div>
                                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Days Present</div>
                                </div>
                                <div style={{ textAlign: 'center' }}>
                                    <div style={{ fontSize: '1.75rem', fontWeight: 'bold', color: '#dc2626', marginBottom: '0.25rem' }}>
                                        {attendanceSummary.absentDays}
                                    </div>
                                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Days Absent</div>
                                </div>
                            </div>
                            <div style={{
                                backgroundColor: 'white',
                                padding: '0.75rem',
                                borderRadius: '6px',
                                textAlign: 'center',
                                fontSize: '0.9rem'
                            }}>
                                Total Days: <strong>{attendanceSummary.totalDays}</strong>
                            </div>
                        </div>
                    </div>

                    <div style={{
                        marginTop: '1.5rem',
                        backgroundColor: 'white',
                        padding: '1rem',
                        borderRadius: '8px',
                        height: '12px',
                        overflow: 'hidden'
                    }}>
                        <div style={{
                            height: '100%',
                            width: `${Math.min(attendanceSummary.attendancePercentage, 100)}%`,
                            backgroundColor: attendanceSummary.attendancePercentage >= 75 ? '#16a34a' : attendanceSummary.attendancePercentage >= 60 ? '#f59e0b' : '#dc2626',
                            borderRadius: '8px',
                            transition: 'width 0.3s ease'
                        }}></div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default EditEmployee;

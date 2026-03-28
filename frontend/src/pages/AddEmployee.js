import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const AddEmployee = () => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [department, setDepartment] = useState('');
    const [salary, setSalary] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);
        
        try {
            await api.post('/api/v1/employees', { 
                name, 
                email, 
                department, 
                salary: parseFloat(salary) 
            });
            setSuccess('Employee record created successfully!');
            // Reset form
            setName('');
            setEmail('');
            setDepartment('');
            setSalary('');
        } catch (err) {
            setError(err.response?.data?.message || 'Initialization failed - Check values');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="add-employee-container" style={{ maxWidth: '600px', margin: '0 auto' }}>
            <header style={{ marginBottom: '2rem' }}>
                <h1 style={{ margin: 0 }}>Register New Employee</h1>
                <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Enter professional details to initialize a new record.</p>
            </header>

            <div className="card">
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>FULL NAME</label>
                            <input 
                                type="text" 
                                placeholder="e.g. John Doe" 
                                value={name} 
                                onChange={(e) => setName(e.target.value)} 
                                required 
                            />
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>EMAIL ADDRESS</label>
                            <input 
                                type="email" 
                                placeholder="name@company.com" 
                                value={email} 
                                onChange={(e) => setEmail(e.target.value)} 
                                required 
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>DEPARTMENT</label>
                            <select 
                                value={department} 
                                onChange={(e) => setDepartment(e.target.value)} 
                                required
                                style={{ width: '100%' }}
                            >
                                <option value="">Select Dept</option>
                                <option value="Engineering">Engineering</option>
                                <option value="Marketing">Marketing</option>
                                <option value="HR">HR</option>
                                <option value="Sales">Sales</option>
                                <option value="Finance">Finance</option>
                                <option value="Operations">Operations</option>
                            </select>
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>ANNUAL SALARY ($)</label>
                            <input 
                                type="number" 
                                placeholder="e.g. 75000" 
                                value={salary} 
                                onChange={(e) => setSalary(e.target.value)} 
                                required 
                            />
                        </div>
                    </div>

                    {error && (
                        <div style={{ color: 'var(--danger)', fontSize: '0.85rem', backgroundColor: '#fef2f2', padding: '0.75rem', borderRadius: '8px', textAlign: 'center' }}>
                            {error}
                        </div>
                    )}
                    {success && (
                        <div style={{ color: 'var(--success)', fontSize: '0.85rem', backgroundColor: '#f0fdf4', padding: '0.75rem', borderRadius: '8px', textAlign: 'center' }}>
                            {success}
                        </div>
                    )}

                    <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                        <button 
                            type="submit" 
                            className="btn-primary" 
                            disabled={loading}
                            style={{ flex: 1, justifyContent: 'center' }}
                        >
                            {loading ? 'Processing...' : 'Create Employee'}
                        </button>
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
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddEmployee;

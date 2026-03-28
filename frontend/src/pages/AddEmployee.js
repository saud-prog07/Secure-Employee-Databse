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
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            await api.post('/api/v1/employees', { name, email, department, salary: parseFloat(salary) });
            setSuccess('Employee added successfully!');
            // Clear form
            setName('');
            setEmail('');
            setDepartment('');
            setSalary('');
        } catch (err) {
            // Priority to backend message (e.g., "Email already exists")
            setError(err.response?.data?.message || err.message || 'Failed to add employee');
        }
    };

    return (
        <div>
            <h2>Add New Employee</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Name:</label>
                    <input type="text" value={name} onChange={(e) => setName(e.target.value)} required />
                </div>
                <div>
                    <label>Email:</label>
                    <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </div>
                <div>
                    <label>Department:</label>
                    <input type="text" value={department} onChange={(e) => setDepartment(e.target.value)} required />
                </div>
                <div>
                    <label>Salary:</label>
                    <input type="number" value={salary} onChange={(e) => setSalary(e.target.value)} required />
                </div>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                {success && <p style={{ color: 'green' }}>{success}</p>}
                <button type="submit">Add Employee</button>
                <button type="button" onClick={() => navigate('/dashboard')} style={{ marginLeft: '10px' }}>Cancel</button>
            </form>
        </div>
    );
};

export default AddEmployee;

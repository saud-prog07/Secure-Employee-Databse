import React, { useState, useEffect } from 'react';
import api from '../services/api';

const PayrollGenerate = () => {
  const [formData, setFormData] = useState({
    employeeId: '',
    baseSalary: '',
    bonus: 0,
    deductions: 0,
    month: new Date().toISOString().slice(0, 7), // YYYY-MM format
  });

  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    // Fetch list of employees for the dropdown
    fetchEmployees();
  }, []);

  const fetchEmployees = async () => {
    try {
      const response = await api.get('/api/v1/employees?page=0&size=1000');
      if (response.data.data && response.data.data.content) {
        setEmployees(response.data.data.content);
      }
    } catch (err) {
      console.error('Error fetching employees:', err);
      setError('Failed to load employees');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'employeeId' ? parseInt(value) : parseFloat(value) || value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!formData.employeeId || !formData.baseSalary) {
      setError('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/api/payroll/generate', {
        employeeId: formData.employeeId,
        baseSalary: parseFloat(formData.baseSalary),
        bonus: parseFloat(formData.bonus) || 0,
        deductions: parseFloat(formData.deductions) || 0,
        month: formData.month,
      });

      if (response.data.status === 'success') {
        setSuccess('Payroll generated successfully!');
        setFormData({
          employeeId: '',
          baseSalary: '',
          bonus: 0,
          deductions: 0,
          month: new Date().toISOString().slice(0, 7),
        });
        setTimeout(() => {
          window.location.href = '/payroll/list';
        }, 1500);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate payroll');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Generate Payroll</h1>

      {error && (
        <div style={{
          backgroundColor: '#fee',
          color: '#c33',
          padding: '1rem',
          borderRadius: '8px',
          marginBottom: '1rem',
          border: '1px solid #fcc'
        }}>
          {error}
        </div>
      )}

      {success && (
        <div style={{
          backgroundColor: '#efe',
          color: '#3c3',
          padding: '1rem',
          borderRadius: '8px',
          marginBottom: '1rem',
          border: '1px solid #cfc'
        }}>
          {success}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600 }}>
            Employee <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <select
            name="employeeId"
            value={formData.employeeId}
            onChange={handleChange}
            required
            style={{
              width: '100%',
              padding: '0.75rem',
              border: '1px solid var(--border)',
              borderRadius: '6px',
              fontSize: '1rem',
            }}
          >
            <option value="">Select an employee</option>
            {employees.map((emp) => (
              <option key={emp.id} value={emp.id}>
                {emp.name} (ID: {emp.id})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600 }}>
            Base Salary <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="number"
            name="baseSalary"
            value={formData.baseSalary}
            onChange={handleChange}
            step="0.01"
            min="0"
            required
            placeholder="Enter base salary"
            style={{
              width: '100%',
              padding: '0.75rem',
              border: '1px solid var(--border)',
              borderRadius: '6px',
              fontSize: '1rem',
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600 }}>
            Bonus
          </label>
          <input
            type="number"
            name="bonus"
            value={formData.bonus}
            onChange={handleChange}
            step="0.01"
            min="0"
            placeholder="Enter bonus (optional)"
            style={{
              width: '100%',
              padding: '0.75rem',
              border: '1px solid var(--border)',
              borderRadius: '6px',
              fontSize: '1rem',
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600 }}>
            Deductions
          </label>
          <input
            type="number"
            name="deductions"
            value={formData.deductions}
            onChange={handleChange}
            step="0.01"
            min="0"
            placeholder="Enter deductions (optional)"
            style={{
              width: '100%',
              padding: '0.75rem',
              border: '1px solid var(--border)',
              borderRadius: '6px',
              fontSize: '1rem',
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600 }}>
            Month <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="month"
            name="month"
            value={formData.month}
            onChange={handleChange}
            required
            style={{
              width: '100%',
              padding: '0.75rem',
              border: '1px solid var(--border)',
              borderRadius: '6px',
              fontSize: '1rem',
            }}
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: loading ? '#ccc' : 'var(--primary)',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            fontSize: '1rem',
            fontWeight: 600,
            cursor: loading ? 'not-allowed' : 'pointer',
            transition: 'background-color 0.3s',
          }}
          onMouseOver={(e) => !loading && (e.target.style.backgroundColor = 'var(--primary-dark)')}
          onMouseOut={(e) => !loading && (e.target.style.backgroundColor = 'var(--primary)')}
        >
          {loading ? 'Generating...' : 'Generate Payroll'}
        </button>
      </form>
    </div>
  );
};

export default PayrollGenerate;

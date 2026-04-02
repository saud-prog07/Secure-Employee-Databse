import React, { useState, useEffect } from 'react';
import api from '../services/api';

const PayrollList = () => {
  const [payrolls, setPayrolls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [size] = useState(10);
  const [employeeFilter, setEmployeeFilter] = useState('');
  const [monthFilter, setMonthFilter] = useState('');

  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  const isAdmin = roles.includes('ROLE_ADMIN');

  useEffect(() => {
    fetchPayrolls();
  }, [page, employeeFilter, monthFilter]);

  const fetchPayrolls = async () => {
    setLoading(true);
    setError('');
    try {
      let url = `/api/payroll?page=${page}&size=${size}&sort=month`;
      
      const response = await api.get(url);
      
      if (response.data.data) {
        let filtered = response.data.data.content || [];
        
        // Client-side filtering
        if (employeeFilter) {
          filtered = filtered.filter(p =>
            p.employeeName.toLowerCase().includes(employeeFilter.toLowerCase())
          );
        }
        
        if (monthFilter) {
          filtered = filtered.filter(p => p.month === monthFilter);
        }

        setPayrolls(filtered);
        setTotalPages(response.data.data.totalPages);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load payroll records');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this payroll record?')) {
      try {
        await api.delete(`/api/payroll/${id}`);
        setSuccess('Payroll deleted successfully');
        setTimeout(() => setSuccess(''), 5000);
        fetchPayrolls();
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to delete payroll');
      }
    }
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading && payrolls.length === 0) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '2rem' }}>
        <p>Loading payroll records...</p>
      </div>
    );
  }

  return (
    <div className="card">
      <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Payroll Records</h1>

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

      {/* Filters */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <input
          type="text"
          placeholder="Filter by employee name..."
          value={employeeFilter}
          onChange={(e) => {
            setEmployeeFilter(e.target.value);
            setPage(0);
          }}
          style={{
            padding: '0.5rem 1rem',
            border: '1px solid var(--border)',
            borderRadius: '6px',
            fontSize: '0.95rem',
            flex: 1,
            minWidth: '200px',
          }}
        />
        <input
          type="month"
          value={monthFilter}
          onChange={(e) => {
            setMonthFilter(e.target.value);
            setPage(0);
          }}
          style={{
            padding: '0.5rem 1rem',
            border: '1px solid var(--border)',
            borderRadius: '6px',
            fontSize: '0.95rem',
            minWidth: '150px',
          }}
        />
        {isAdmin && (
          <button
            onClick={() => window.location.href = '/payroll/generate'}
            style={{
              padding: '0.5rem 1.5rem',
              backgroundColor: 'var(--primary)',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              fontSize: '0.95rem',
              fontWeight: 600,
              cursor: 'pointer',
              whiteSpace: 'nowrap',
            }}
          >
            + New Payroll
          </button>
        )}
      </div>

      {/* Table */}
      {payrolls.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem' }}>
          No payroll records found
        </p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table style={{
            width: '100%',
            borderCollapse: 'collapse',
            fontSize: '0.95rem',
          }}>
            <thead>
              <tr style={{ backgroundColor: 'var(--bg-secondary)', borderBottom: '2px solid var(--border)' }}>
                <th style={{ padding: '1rem', textAlign: 'left', fontWeight: 600 }}>Employee</th>
                <th style={{ padding: '1rem', textAlign: 'center', fontWeight: 600 }}>Month</th>
                <th style={{ padding: '1rem', textAlign: 'right', fontWeight: 600 }}>Base Salary</th>
                <th style={{ padding: '1rem', textAlign: 'right', fontWeight: 600 }}>Bonus</th>
                <th style={{ padding: '1rem', textAlign: 'right', fontWeight: 600 }}>Deductions</th>
                <th style={{ padding: '1rem', textAlign: 'right', fontWeight: 600 }}>Final Salary</th>
                <th style={{ padding: '1rem', textAlign: 'center', fontWeight: 600 }}>Generated</th>
                {isAdmin && <th style={{ padding: '1rem', textAlign: 'center', fontWeight: 600 }}>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {payrolls.map((payroll) => (
                <tr key={payroll.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '1rem' }}>{payroll.employeeName}</td>
                  <td style={{ padding: '1rem', textAlign: 'center' }}>{payroll.month}</td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>{formatCurrency(payroll.baseSalary)}</td>
                  <td style={{ padding: '1rem', textAlign: 'right', color: '#27ae60' }}>{formatCurrency(payroll.bonus)}</td>
                  <td style={{ padding: '1rem', textAlign: 'right', color: '#e74c3c' }}>{formatCurrency(payroll.deductions)}</td>
                  <td style={{ padding: '1rem', textAlign: 'right', fontWeight: 600, color: 'var(--primary)' }}>
                    {formatCurrency(payroll.finalSalary)}
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    {formatDate(payroll.createdAt)}
                  </td>
                  {isAdmin && (
                    <td style={{ padding: '1rem', textAlign: 'center' }}>
                      <button
                        onClick={() => handleDelete(payroll.id)}
                        style={{
                          padding: '0.4rem 0.8rem',
                          backgroundColor: '#f1f5f9',
                          color: '#e74c3c',
                          border: '1px solid #e74c3c',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '0.85rem',
                          fontWeight: 600,
                        }}
                      >
                        Delete
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          gap: '1rem',
          marginTop: '1.5rem',
          flexWrap: 'wrap',
        }}>
          <button
            onClick={() => setPage(Math.max(page - 1, 0))}
            disabled={page === 0}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: page === 0 ? '#ccc' : 'var(--primary)',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              cursor: page === 0 ? 'not-allowed' : 'pointer',
            }}
          >
            Previous
          </button>
          <span style={{ alignSelf: 'center', fontWeight: 600 }}>
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage(Math.min(page + 1, totalPages - 1))}
            disabled={page === totalPages - 1}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: page === totalPages - 1 ? '#ccc' : 'var(--primary)',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              cursor: page === totalPages - 1 ? 'not-allowed' : 'pointer',
            }}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default PayrollList;

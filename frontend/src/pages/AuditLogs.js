import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';

const AuditLogs = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const fetchLogs = async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/admin/audit');
            setLogs(response.data.data);
            setError(null);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch audit logs');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
    }, []);

    return (
        <div className="audit-container">
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <div>
                    <h1 style={{ margin: 0 }}>System Audit Logs</h1>
                    <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>Traceability of sensitive administrative actions</p>
                </div>
                <button onClick={() => navigate('/dashboard')} style={{ backgroundColor: '#f1f5f9', color: '#475569' }}>
                    Back to Dashboard
                </button>
            </header>

            {error && (
                <div className="card" style={{ border: '1px solid var(--danger)', backgroundColor: '#fef2f2', color: 'var(--danger)', marginBottom: '1rem' }}>
                    {error}
                </div>
            )}
            
            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading logs...
                    </div>
                ) : logs.length === 0 ? (
                    <div style={{ padding: '4rem', textAlign: 'center' }}>
                        <h3 style={{ margin: 0 }}>No audit logs found</h3>
                        <p style={{ color: 'var(--text-muted)' }}>Administrative actions will appear here once performed.</p>
                    </div>
                ) : (
                    <table style={{ margin: 0 }}>
                        <thead style={{ backgroundColor: '#f8fafc' }}>
                            <tr>
                                <th>Timestamp</th>
                                <th>Action Type</th>
                                <th>Performed By</th>
                            </tr>
                        </thead>
                        <tbody>
                            {logs.map(log => (
                                <tr key={log.id}>
                                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                                        {new Date(log.timestamp).toLocaleString()}
                                    </td>
                                    <td>
                                        <span className="badge" style={{ backgroundColor: '#eff6ff', color: 'var(--primary)', border: '1px solid #dbeafe' }}>
                                            {log.action}
                                        </span>
                                    </td>
                                    <td style={{ fontWeight: 600 }}>{log.username}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default AuditLogs;

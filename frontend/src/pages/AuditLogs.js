import React, { useEffect, useState } from 'react';
import api from '../services/api';

const AuditLogs = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchLogs = async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/admin/audit');
            setLogs(response.data.data);
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
        <div>
            <h2>System Audit Logs (Admin Only)</h2>
            {loading && <p>Loading logs...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
            
            {!loading && !error && (
                <table border="1" style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse', marginTop: '20px' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#f2f2f2' }}>
                            <th style={{ padding: '10px' }}>Timestamp</th>
                            <th>Action</th>
                            <th>Performed By</th>
                        </tr>
                    </thead>
                    <tbody>
                        {logs.map(log => (
                            <tr key={log.id}>
                                <td style={{ padding: '10px' }}>{new Date(log.timestamp).toLocaleString()}</td>
                                <td style={{ fontWeight: 'bold' }}>{log.action}</td>
                                <td>{log.username}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default AuditLogs;

import React, { useState, useEffect } from 'react';
import api from '../services/api';

const QRCodeDisplay = ({ employeeId }) => {
    const [qrCode, setQrCode] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchQRCode = async () => {
            try {
                setLoading(true);
                const response = await api.get(`/api/v1/employees/${employeeId}/qr/data-url`);
                setQrCode(response.data.data.qrCodeUrl);
                setError(null);
            } catch (err) {
                setError('Failed to load QR code');
                console.error('QR Code error:', err);
            } finally {
                setLoading(false);
            }
        };
        
        if (employeeId) {
            fetchQRCode();
        }
    }, [employeeId]);

    const handleDownload = async () => {
        try {
            const response = await api.get(`/api/v1/employees/${employeeId}/qr`, {
                responseType: 'blob'
            });
            
            const url = window.URL.createObjectURL(response.data);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `employee-qr-${employeeId}.png`);
            document.body.appendChild(link);
            link.click();
            link.parentElement.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error('Download error:', err);
            setError('Failed to download QR code');
        }
    };

    if (loading) {
        return <div style={{ textAlign: 'center', padding: '1rem' }}>Generating QR code...</div>;
    }

    if (error) {
        return <div style={{ color: 'var(--danger)', textAlign: 'center', padding: '1rem' }}>{error}</div>;
    }

    return (
        <div style={{ textAlign: 'center' }}>
            {qrCode && (
                <>
                    <img 
                        src={qrCode} 
                        alt={`QR Code for Employee ${employeeId}`} 
                        style={{ 
                            width: '200px', 
                            height: '200px',
                            padding: '1rem',
                            border: '2px solid #e2e8f0',
                            borderRadius: '8px',
                            marginBottom: '1rem'
                        }} 
                    />
                    <div style={{ marginTop: '1rem' }}>
                        <button 
                            onClick={handleDownload}
                            className="btn-primary"
                            style={{
                                padding: '0.5rem 1rem',
                                fontSize: '0.9rem',
                                cursor: 'pointer'
                            }}
                        >
                            Download QR Code
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

export default QRCodeDisplay;

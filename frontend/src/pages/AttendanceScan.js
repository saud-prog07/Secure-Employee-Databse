import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { storage } from '../utils/storage';
import './Attendance.css';

const AttendanceScan = () => {
  const [username, setUsername] = useState('');
  const [employeeId, setEmployeeId] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [loading, setLoading] = useState(false);
  const [isScannerVisible, setIsScannerVisible] = useState(false);
  const [isScannerInitialized, setIsScannerInitialized] = useState(false);
  const [showManualEntry, setShowManualEntry] = useState(false);
  const [hasCheckedIn, setHasCheckedIn] = useState(false);
  const [userRole, setUserRole] = useState('');
  const navigate = useNavigate();
  const scannerRef = useRef(null);
  const scannerInitializedRef = useRef(false);

  useEffect(() => {
    const token = storage.get('token');
    const guestMode = localStorage.getItem('guestMode');
    
    if (token || guestMode) {
      const storedUsername = storage.get('username') || localStorage.getItem('username');
      setUsername(storedUsername || 'Guest');
      
      // Get user role
      const rolesString = storage.get('roles');
      let userRoles = [];
      try {
        userRoles = rolesString ? JSON.parse(rolesString) : [];
      } catch (e) {
        console.error('Failed to parse roles:', rolesString);
        userRoles = [];
      }
      
      const cleanRoles = userRoles.map(role => role.toString().replace('ROLE_', '').toUpperCase());
      if (cleanRoles.includes('EMPLOYEE')) {
        setUserRole('EMPLOYEE');
      } else if (cleanRoles.includes('ADMIN')) {
        setUserRole('ADMIN');
      } else if (cleanRoles.includes('HR')) {
        setUserRole('HR');
      }
    } else {
      navigate('/login');
    }
  }, [navigate]);

  // Initialize scanner when element exists (useEffect approach)
  useEffect(() => {
    if (isScannerVisible && !scannerInitializedRef.current) {
      // Wait for DOM to render the element
      setTimeout(() => {
        const readerElement = document.getElementById('reader');
        if (readerElement && !scannerInitializedRef.current) {
          try {
            const scanner = new Html5QrcodeScanner('reader', {
              fps: 10,
              qrbox: { width: 250, height: 250 },
              rememberLastUsedCamera: true,
              showTorchButtonIfSupported: true
            });

            scanner.render(onScanSuccess, onScanFailure);
            scannerRef.current = scanner;
            scannerInitializedRef.current = true;
            setIsScannerInitialized(true);
          } catch (error) {
            console.error('Failed to initialize scanner:', error);
            setMessage('Failed to initialize camera. Please try again.');
            setMessageType('error');
          }
        }
      }, 100);
    }

    return () => {
      // Cleanup is handled in cleanupScanner
    };
  }, [isScannerVisible]);

  const cleanupScanner = () => {
    if (scannerRef.current) {
      scannerRef.current.clear().catch(error => {
        console.error('Failed to clear scanner:', error);
      });
      scannerRef.current = null;
    }
    scannerInitializedRef.current = false;
    setIsScannerInitialized(false);
  };

  // Handle scanner toggle
  const handleToggleScanner = () => {
    if (isScannerVisible) {
      cleanupScanner();
      setIsScannerVisible(false);
    } else {
      setIsScannerVisible(true);
    }
  };

  const onScanSuccess = (decodedText) => {
    console.log('Scan Success:', decodedText);
    setEmployeeId(decodedText);
    // Automatically trigger attendance recording on scan
    recordAttendance(decodedText);
  };

  const onScanFailure = (error) => {
    // Suppress error spam
  };

  const recordAttendance = async (id) => {
    const targetId = id || employeeId;
    if (!targetId) {
      setMessage('Please scan or enter employee ID');
      setMessageType('error');
      return;
    }

    setLoading(true);
    try {
      const token = storage.get('token');
      const res = await axios.post('http://localhost:8080/api/attendance/scan', {
        employeeId: targetId,
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (res.data.status === 'success') {
        setMessage(`✓ Checked in successfully`);
        setMessageType('success');
        setEmployeeId('');
        setHasCheckedIn(true);
        
        setTimeout(() => {
          setMessage('');
        }, 5000);
      }
    } catch (err) {
      console.error('Scan error:', err);
      const errorMsg = err.response?.data?.message || 'Error recording attendance';
      
      // Check if error is "already checked in today"
      if (errorMsg.toLowerCase().includes('already') || errorMsg.toLowerCase().includes('today')) {
        setMessage(`⚠ ${errorMsg}`);
        setMessageType('warning');
        setHasCheckedIn(true);
      } else {
        setMessage(errorMsg);
        setMessageType('error');
      }
    } finally {
      setLoading(false);
    }
  };

  // Handle Manual Attendance Submit
  const handleManualSubmit = (e) => {
    e.preventDefault();
    recordAttendance();
  };

  // Handle Demo Scan - Simulate QR scan with dummy ID
  const handleDemoScan = async () => {
    console.log('Demo mode: Simulating scan with EMP001');
    recordAttendance('EMP001');
  };

  // Handle Logout
  const handleLogout = () => {
    cleanupScanner();
    storage.clearAuth();
    localStorage.removeItem('guestMode');
    setUsername('');
    setEmployeeId('');
    setMessage('');
    navigate('/login');
  };

  return (
    <div className="attendance-container-employee">
      <div className="attendance-card-employee">
        {/* Header */}
        <div className="employee-header">
          <div className="employee-status">
            <div className={`status-indicator ${hasCheckedIn ? 'logged-in' : 'logged-out'}`}></div>
            <div className="status-text">
              <p className="status-label">Logged in as</p>
              <p className="username-display">{username}</p>
              {userRole && <p className="user-role-badge">{userRole}</p>}
            </div>
          </div>
          <button onClick={handleLogout} className="btn-logout-employee">
            Logout
          </button>
        </div>

        {/* Message Display */}
        {message && (
          <div className={`message-employee ${messageType === 'success' ? 'success' : messageType === 'warning' ? 'warning' : 'error'}`}>
            <p className="message-text">{message}</p>
          </div>
        )}

        {/* Dashboard Option for HR/ADMIN after check-in */}
        {hasCheckedIn && (userRole === 'HR' || userRole === 'ADMIN') && (
          <div className="dashboard-option-card">
            <p className="option-text">✓ Attendance recorded</p>
            <button 
              onClick={() => navigate('/dashboard')}
              className="btn-navigate-dashboard"
            >
              → Go to Dashboard
            </button>
            <p className="option-hint">Or stay here to help others check in</p>
          </div>
        )}

        {/* Main Scanner Button */}
        <div className="scanner-button-section">
          <button 
            onClick={handleToggleScanner}
            className={`btn-scan-qr ${isScannerVisible ? 'active' : ''}`}
            disabled={loading}
          >
            {isScannerVisible ? '✕ Close Scanner' : '📱 Scan QR Code'}
          </button>
          <p className="scanner-instruction">
            {isScannerVisible ? 'Position QR code in frame' : userRole === 'EMPLOYEE' ? 'Scan your QR code' : `Scan ${userRole.toLowerCase()}'s or employee's QR code`}
          </p>
        </div>

        {/* QR Scanner - Hidden by Default */}
        {isScannerVisible && (
          <div className="scanner-section-employee">
            <div id="reader" className="qr-reader-container"></div>
          </div>
        )}

        {/* Fallback Manual Entry */}
        <div className="fallback-section">
          {!showManualEntry ? (
            <div className="fallback-buttons">
              <button 
                onClick={() => setShowManualEntry(true)}
                className="btn-fallback"
                disabled={isScannerVisible || loading}
              >
                📝 {userRole === 'EMPLOYEE' ? 'Manual Check-in' : 'Manual Entry'}
              </button>
              <button 
                onClick={handleDemoScan}
                className="btn-demo"
                disabled={isScannerVisible || loading}
                title="Simulate QR scan with demo employee ID (EMP001)"
              >
                🎬 Simulate Scan
              </button>
            </div>
          ) : (
            <form onSubmit={handleManualSubmit} className="manual-form">
              <div className="form-group-employee">
                <label className="manual-entry-label">
                  {userRole === 'EMPLOYEE' ? 'Your Employee ID' : 'Enter Employee ID'}
                </label>
                <input
                  type="text"
                  placeholder={userRole === 'EMPLOYEE' ? 'e.g., EMP001' : 'e.g., EMP001'}
                  value={employeeId}
                  onChange={(e) => setEmployeeId(e.target.value)}
                  className="input-field-employee"
                  disabled={loading}
                  autoFocus
                />
              </div>
              <div className="button-group-employee">
                <button type="submit" className="btn-submit" disabled={loading}>
                  {loading ? 'Checking in...' : userRole === 'EMPLOYEE' ? 'Check In' : 'Record Attendance'}
                </button>
                <button 
                  type="button" 
                  onClick={() => {
                    setShowManualEntry(false);
                    setEmployeeId('');
                    setMessage('');
                  }}
                  className="btn-cancel"
                >
                  Cancel
                </button>
              </div>
              {(userRole === 'HR' || userRole === 'ADMIN') && (
                <p className="manual-hint">You can record attendance for your own ID or scan multiple employee IDs</p>
              )}
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default AttendanceScan;

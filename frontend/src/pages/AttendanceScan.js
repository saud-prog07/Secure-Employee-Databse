import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Attendance.css';

const AttendanceScan = () => {
  const [username, setUsername] = useState('');
  const [employeeId, setEmployeeId] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const guestMode = localStorage.getItem('guestMode');
    
    if (token || guestMode) {
      const storedUsername = localStorage.getItem('username');
      setUsername(storedUsername || 'Guest');
    } else {
      navigate('/login');
    }
  }, [navigate]);

  // Handle Attendance Scan
  const handleScanAttendance = async (e) => {
    e.preventDefault();
    if (!employeeId) {
      setMessage('Please enter employee ID');
      setMessageType('error');
      return;
    }

    setLoading(true);
    try {
      const res = await axios.post('http://localhost:8080/api/attendance/scan', {
        employeeId: parseInt(employeeId),
      });

      if (res.data.data) {
        setMessage('✓ Attendance recorded successfully!');
        setMessageType('success');
        setEmployeeId('');

        setTimeout(() => {
          setMessage('');
        }, 4000);
      }
    } catch (err) {
      console.error('Scan error:', err);
      setMessage(err.response?.data?.message || 'Error scanning attendance');
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  // Handle Check Status
  const handleCheckStatus = async (e) => {
    e.preventDefault();
    if (!employeeId) {
      setMessage('Please enter employee ID');
      setMessageType('error');
      return;
    }

    setLoading(true);
    try {
      const res = await axios.get(`http://localhost:8080/api/attendance/status/${employeeId}`);
      
      if (res.data.data) {
        if (res.data.data.loginTime) {
          setMessage(`✓ Logged in at ${res.data.data.loginTime}`);
        } else {
          setMessage('Not logged in yet today');
        }
        setMessageType('success');
      }
    } catch (err) {
      console.error('Status check error:', err);
      setMessage('Employee not found or not logged in');
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  // Handle Logout
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('guestMode');
    setUsername('');
    setEmployeeId('');
    setMessage('');
    navigate('/login');
  };

  return (
    <div className="attendance-container">
      <div className="attendance-card">
        {/* Header with Logout Button */}
        <div className="attendance-header">
          <div className="header-left">
            <h1>Employee Attendance</h1>
            <p className="logged-in-as">Logged in: <strong>{username}</strong></p>
          </div>
          <button
            onClick={handleLogout}
            className="btn btn-logout"
          >
            Logout
          </button>
        </div>

        {/* Simple Message Display */}
        {message && (
          <div className={`message ${messageType === 'success' ? 'success' : 'error'}`}>
            <p className="message-text">{message}</p>
            <button
              onClick={() => setMessage('')}
              className="btn-close-message"
            >
              ×
            </button>
          </div>
        )}

        {/* SCANNING FORM ONLY */}
        <div className="scan-section">
          <p className="attendance-subtitle">
            Enter your employee ID to log attendance
          </p>

          <form onSubmit={handleScanAttendance} className="attendance-form">
            <div className="form-group">
              <input
                type="text"
                placeholder="Enter employee ID or scan QR code"
                value={employeeId}
                onChange={(e) => setEmployeeId(e.target.value)}
                className="input-field"
                autoFocus
                disabled={loading}
              />
            </div>

            <div className="button-group">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading}
              >
                {loading ? 'Processing...' : 'Scan Attendance'}
              </button>
              <button
                type="button"
                onClick={handleCheckStatus}
                className="btn btn-secondary"
                disabled={loading}
              >
                {loading ? 'Checking...' : 'Check Status'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AttendanceScan;

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Attendance.css';

const Attendance = () => {
  // Login state
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);

  // Attendance state
  const [employeeId, setEmployeeId] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState(''); // 'success' or 'error'
  const [employeeName, setEmployeeName] = useState('');
  const [loginTime, setLoginTime] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Check if user is already logged in
  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (token && userData) {
      setIsLoggedIn(true);
      setUser(JSON.parse(userData));
    }
  }, []);

  // Handle Employee Login
  const handleEmployeeLogin = async (e) => {
    e.preventDefault();
    if (!username || !password) {
      setMessage('Please enter username and password');
      setMessageType('error');
      return;
    }

    setLoading(true);
    try {
      const res = await axios.post('http://localhost:8080/api/v1/auth/login', {
        username: username,
        password: password,
      });

      if (res.data.data && res.data.data.twoFactorEnabled) {
        localStorage.setItem('tempUsername', username);
        navigate('/verify-otp');
        return;
      }

      const token = res.data.data.token;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(res.data.data));
      
      setIsLoggedIn(true);
      setUser(res.data.data);
      setUsername('');
      setPassword('');
      setMessage(`Welcome ${res.data.data.fullName}! Ready to scan attendance.`);
      setMessageType('success');
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error('Login error:', err);
      setMessage(err.response?.data?.message || 'Login failed. Please check your credentials.');
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

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
        setEmployeeName(res.data.data.employeeName);
        setLoginTime(res.data.data.loginTime);
        setMessage(`${res.data.data.employeeName} logged in successfully at ${res.data.data.loginTime}`);
        setMessageType('success');
        setEmployeeId('');

        setTimeout(() => {
          setMessage('');
          setEmployeeName('');
          setLoginTime('');
        }, 5000);
      }
    } catch (err) {
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
        setEmployeeName(res.data.data.employeeName);
        if (res.data.data.loginTime) {
          setLoginTime(res.data.data.loginTime);
        }
        setMessage(`${res.data.data.employeeName} - ${res.data.data.status}`);
        setMessageType('success');
      }
    } catch (err) {
      setMessage(err.response?.data?.message || 'Error checking status');
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  // Handle Logout
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsLoggedIn(false);
    setUser(null);
    setUsername('');
    setPassword('');
    setEmployeeId('');
    setMessage('Logged out successfully');
    setMessageType('success');
    setTimeout(() => setMessage(''), 3000);
  };

  return (
    <div className="attendance-container">
      <div className="attendance-card">
        {/* Header with Logout Button */}
        <div className="attendance-header">
          <h1>{isLoggedIn ? 'Attendance Scan' : 'Employee Login'}</h1>
          {isLoggedIn && (
            <button
              onClick={handleLogout}
              className="btn-logout"
            >
              Logout
            </button>
          )}
        </div>

        {/* Message Display */}
        {message && (
          <div className={`message ${messageType === 'success' ? 'success' : 'error'}`}>
            <div className="message-icon">
              {messageType === 'success' ? '✓' : '✗'}
            </div>
            <div className="message-content">
              <p className="message-text">{message}</p>
              {employeeName && (
                <div className="attendance-details">
                  <p><strong>Employee:</strong> {employeeName}</p>
                  {loginTime && (
                    <p><strong>Time:</strong> {loginTime}</p>
                  )}
                </div>
              )}
            </div>
            <button
              onClick={() => setMessage('')}
              className="btn-close-message"
            >
              ×
            </button>
          </div>
        )}

        {/* LOGIN FORM - Show only if not logged in */}
        {!isLoggedIn ? (
          <>
            <p className="attendance-subtitle">
              Enter your credentials to continue to attendance scanning
            </p>

            <form onSubmit={handleEmployeeLogin} className="attendance-form">
              <div className="form-group">
                <label htmlFor="username">Username:</label>
                <input
                  id="username"
                  type="text"
                  placeholder="Enter your username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="input-field"
                  autoFocus
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="password">Password:</label>
                <input
                  id="password"
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="input-field"
                  disabled={loading}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading}
              >
                {loading ? 'Logging in...' : 'Login'}
              </button>
            </form>

            <div className="divider-line"></div>

            <p className="continuation-text">
              Just scanning without login?
            </p>

            <button
              onClick={() => setIsLoggedIn('guest')}
              className="btn btn-secondary"
            >
              Continue as Guest (Scan Only)
            </button>

            <div className="attendance-info">
              <h3>About Attendance Scanning:</h3>
              <ul>
                <li>Login with your credentials for full access</li>
                <li>Or continue as guest for quick scanning</li>
                <li>Enter your Employee ID to log attendance</li>
                <li>You can only log in once per day</li>
              </ul>
            </div>
          </>
        ) : (
          <>
            {/* LOGGED IN - Show user info and attendance scanning */}
            <div className="user-info-box">
              <p className="user-name">Logged in as: <strong>{user?.fullName}</strong></p>
              <p className="user-role">{user?.role || 'Employee'}</p>
            </div>

            <p className="attendance-subtitle">
              Enter employee ID to log attendance
            </p>

            <form onSubmit={handleScanAttendance} className="attendance-form">
              <div className="form-group">
                <label htmlFor="employeeId">Employee ID:</label>
                <input
                  id="employeeId"
                  type="number"
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
                  {loading ? 'Scanning...' : 'Scan Attendance'}
                </button>
                <button
                  onClick={handleCheckStatus}
                  className="btn btn-secondary"
                  disabled={loading}
                  type="button"
                >
                  {loading ? 'Checking...' : 'Check Status'}
                </button>
              </div>
            </form>

            <div className="attendance-info">
              <h3>How to Use:</h3>
              <ol>
                <li>Enter employee's 4-digit ID number</li>
                <li>Click "Scan Attendance" or press Enter</li>
                <li>You can only log in once per day (auto-prevents duplicates)</li>
                <li>Use "Check Status" to verify login time</li>
              </ol>
            </div>

            <div className="divider-line"></div>

            <button
              onClick={() => navigate('/dashboard')}
              className="btn btn-back"
            >
              Go to Dashboard
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default Attendance;

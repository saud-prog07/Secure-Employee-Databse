import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate, useNavigate, useLocation } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import OtpVerification from './pages/OtpVerification';
import Dashboard from './pages/Dashboard';
import AddEmployee from './pages/AddEmployee';
import ManageUsers from './pages/ManageUsers';
import AuditLogs from './pages/AuditLogs';
import Profile from './pages/Profile';
import EditEmployee from './pages/EditEmployee';
import PayrollGenerate from './pages/PayrollGenerate';
import PayrollList from './pages/PayrollList';
import AttendanceScan from './pages/AttendanceScan';
import EmployeeWorkday from './pages/EmployeeWorkday';

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const token = localStorage.getItem('token');
  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  const isAdmin = roles.includes('ROLE_ADMIN');

  if (!token || location.pathname === '/' || location.pathname === '/login' || location.pathname === '/register' || location.pathname === '/verify-otp' || location.pathname === '/attendance-scan') return null;

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('username');
    navigate('/');
  };

  const NavLink = ({ to, children, adminOnly = false }) => {
    if (adminOnly && !isAdmin) return null;
    const isActive = location.pathname === to;
    return (
      <Link 
        to={to} 
        style={{ 
          color: isActive ? 'var(--primary)' : 'var(--text-muted)',
          textDecoration: 'none',
          fontWeight: isActive ? 600 : 500,
          borderBottom: isActive ? '2px solid var(--primary)' : '2px solid transparent',
          padding: '0.5rem 0'
        }}
      >
        {children}
      </Link>
    );
  };

  return (
    <nav className="card" style={{ 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'space-between', 
      padding: '0.75rem 2rem',
      marginBottom: '2rem',
      position: 'sticky',
      top: '1rem',
      zIndex: 100
    }}>
      <div style={{ display: 'flex', gap: '2rem', alignItems: 'center' }}>
        <div style={{ fontWeight: 800, color: 'var(--primary)', fontSize: '1.25rem', marginRight: '1rem' }}>
          EMS<span style={{ color: 'var(--slate-700)' }}>.io</span>
        </div>
        <NavLink to="/dashboard">Dashboard</NavLink>
        <NavLink to="/attendance-scan">Attendance</NavLink>
        <NavLink to="/add">Add Employee</NavLink>
        <NavLink to="/workday" adminOnly>Employee Workday</NavLink>
        <NavLink to="/payroll/list" adminOnly>Payroll</NavLink>
        <NavLink to="/admin/users" adminOnly>Manage Users</NavLink>
        <NavLink to="/admin/audit" adminOnly>Audit Logs</NavLink>
        <NavLink to="/profile">My Profile</NavLink>
      </div>
      <button 
        onClick={handleLogout}
        style={{ 
          backgroundColor: '#f1f5f9', 
          color: '#475569', 
          border: 'none', 
          padding: '0.5rem 1rem',
          borderRadius: '8px',
          fontWeight: 600
        }}
      >
        Logout
      </button>
    </nav>
  );
};

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  
  if (!token) {
    console.warn('[PrivateRoute] No token found - redirecting to login');
    return <Navigate to="/" replace />;
  }
  
  console.log('[PrivateRoute] Token present - allowing access');
  return children;
};

const AdminRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  const rolesStr = localStorage.getItem('roles');
  const roles = rolesStr ? JSON.parse(rolesStr) : [];
  const isAdmin = roles.includes('ROLE_ADMIN');
  
  console.log('[AdminRoute] Checking admin access', { hasToken: !!token, roles, isAdmin });
  
  if (!token) {
    console.warn('[AdminRoute] No token found - redirecting to login');
    return <Navigate to="/" replace />;
  }
  
  if (!isAdmin) {
    console.warn('[AdminRoute] Not admin - redirecting to dashboard');
    return <Navigate to="/dashboard" replace />;
  }
  
  return children;
};

function App() {
  return (
    <Router>
      <div className="container">
        <Navbar />
        <main>
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/attendance-scan" element={<AttendanceScan />} />
            <Route path="/verify-otp" element={<OtpVerification />} />
            <Route 
              path="/dashboard" 
              element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/add" 
              element={
                <PrivateRoute>
                  <AddEmployee />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/admin/users" 
              element={
                <PrivateRoute>
                    <AdminRoute>
                        <ManageUsers />
                    </AdminRoute>
                </PrivateRoute>
              } 
            />
            <Route 
              path="/admin/audit" 
              element={
                <PrivateRoute>
                    <AdminRoute>
                        <AuditLogs />
                    </AdminRoute>
                </PrivateRoute>
              } 
            />
            <Route 
              path="/profile" 
              element={
                <PrivateRoute>
                  <Profile />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/edit/:id" 
              element={
                <PrivateRoute>
                  <EditEmployee />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/payroll/list" 
              element={
                <PrivateRoute>
                  <PayrollList />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/payroll/generate" 
              element={
                <PrivateRoute>
                    <AdminRoute>
                        <PayrollGenerate />
                    </AdminRoute>
                </PrivateRoute>
              } 
            />
            <Route 
              path="/workday" 
              element={
                <PrivateRoute>
                  <EmployeeWorkday />
                </PrivateRoute>
              } 
            />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate, useNavigate, useLocation } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import AddEmployee from './pages/AddEmployee';
import ManageUsers from './pages/ManageUsers';
import AuditLogs from './pages/AuditLogs';
import Profile from './pages/Profile';

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const token = localStorage.getItem('token');
  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  const isAdmin = roles.includes('ROLE_ADMIN');

  if (!token || location.pathname === '/') return null;

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
        <NavLink to="/add">Add Employee</NavLink>
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
  return token ? children : <Navigate to="/" />;
};

const AdminRoute = ({ children }) => {
  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  const isAdmin = roles.includes('ROLE_ADMIN');
  return isAdmin ? children : <Navigate to="/dashboard" />;
};

function App() {
  return (
    <Router>
      <div className="container">
        <Navbar />
        <main>
          <Routes>
            <Route path="/" element={<Login />} />
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
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;

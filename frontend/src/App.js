import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate, useNavigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import AddEmployee from './pages/AddEmployee';
import ManageUsers from './pages/ManageUsers';
import AuditLogs from './pages/AuditLogs';

const Navbar = () => {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  const isAdmin = roles.includes('ROLE_ADMIN');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('username');
    navigate('/');
  };

  if (!token) return null;

  return (
    <nav style={{ padding: '10px', backgroundColor: '#eee', marginBottom: '20px' }}>
      <Link to="/dashboard" style={{ marginRight: '15px' }}>Dashboard</Link>
      <Link to="/add" style={{ marginRight: '15px' }}>Add Employee</Link>
      {isAdmin && (
        <>
          <Link to="/admin/users" style={{ marginRight: '15px', fontWeight: 'bold' }}>Manage Users</Link>
          <Link to="/admin/audit" style={{ marginRight: '15px', fontWeight: 'bold' }}>Audit Logs</Link>
        </>
      )}
      <button onClick={handleLogout}>Logout</button>
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
      <div className="App" style={{ padding: '20px' }}>
        <header>
          <h1>Secure Employee Management</h1>
        </header>
        
        <Navbar />

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
              <AdminRoute>
                <ManageUsers />
              </AdminRoute>
            } 
          />
          <Route 
            path="/admin/audit" 
            element={
              <AdminRoute>
                <AuditLogs />
              </AdminRoute>
            } 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { storage } from './utils/storage';
import Login from './pages/Login';
import Register from './pages/Register';
import OtpVerification from './pages/OtpVerification';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
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
import ProtectedRoute from './components/ProtectedRoute';
import Sidebar from './components/Sidebar';

const PrivateRoute = ({ children }) => {
  const token = storage.get('token');
  
  if (!token) {
    return <Navigate to="/" replace />;
  }
  
  return children;
};

function AppContent() {
  const location = useLocation();
  const token = storage.get('token');
  
  // Get user roles
  const rolesString = storage.get('roles');
  let userRoles = [];
  try {
    userRoles = rolesString ? JSON.parse(rolesString) : [];
  } catch (e) {
    userRoles = [];
  }
  
  // Convert roles to clean format
  const cleanRoles = userRoles.map(role => role.toString().replace('ROLE_', '').toUpperCase());
  const isEmployee = cleanRoles.includes('EMPLOYEE');
  
  // Hide sidebar for EMPLOYEE role and for auth pages
  const showSidebar = token && !isEmployee && !['/login', '/', '/register', '/verify-otp', '/attendance-scan'].includes(location.pathname);

  return (
    <div className="app-layout">
      {showSidebar && <Sidebar />}
      <div className={`main-content ${showSidebar ? 'with-sidebar' : ''}`}>
          <main>
            <Routes>
              <Route path="/" element={<Login />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />
              <Route path="/verify-otp" element={<OtpVerification />} />
              
              {/* EMPLOYEE ONLY - Attendance Scanning */}
              <Route 
                path="/attendance-scan" 
                element={
                  <ProtectedRoute allowedRoles={['EMPLOYEE']}>
                    <AttendanceScan />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN & HR - Dashboard */}
              <Route 
                path="/dashboard" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'HR']}>
                    <Dashboard />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN ONLY - Add Employee */}
              <Route 
                path="/add" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AddEmployee />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN ONLY - Edit Employee */}
              <Route 
                path="/edit/:id" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <EditEmployee />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN ONLY - Manage Users */}
              <Route 
                path="/admin/users" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <ManageUsers />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN & HR - Audit Logs */}
              <Route 
                path="/admin/audit" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'HR']}>
                    <AuditLogs />
                  </ProtectedRoute>
                } 
              />

              {/* ALL AUTHENTICATED - Profile */}
              <Route 
                path="/profile" 
                element={
                  <ProtectedRoute>
                    <Profile />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN & HR - Payroll List */}
              <Route 
                path="/payroll/list" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'HR']}>
                    <PayrollList />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN ONLY - Generate Payroll */}
              <Route 
                path="/payroll/generate" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <PayrollGenerate />
                  </ProtectedRoute>
                } 
              />

              {/* ADMIN & HR - Employee Workday */}
              <Route 
                path="/workday" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'HR']}>
                    <EmployeeWorkday />
                  </ProtectedRoute>
                } 
              />

              {/* Catch all - redirect to login */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </div>
    );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;

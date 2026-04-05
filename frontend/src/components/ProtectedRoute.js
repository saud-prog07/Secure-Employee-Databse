import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  const token = localStorage.getItem('token');
  const rolesString = localStorage.getItem('roles');
  const roles = rolesString ? JSON.parse(rolesString) : [];

  // Convert roles to clean format (remove ROLE_ prefix)
  const cleanRoles = roles.map(role => role.replace('ROLE_', ''));

  // Check if user is authenticated
  if (!token) {
    console.log('ProtectedRoute: No token found, redirecting to login');
    return <Navigate to="/login" replace />;
  }

  // CRITICAL: Strict EMPLOYEE role enforcement
  // EMPLOYEE users can ONLY access /attendance-scan
  // Any attempt to access other routes should redirect to /attendance-scan
  if (cleanRoles.includes('EMPLOYEE')) {
    console.log('ProtectedRoute: EMPLOYEE user detected, roles:', cleanRoles);
    
    // If this route doesn't allow EMPLOYEE role, block and redirect to attendance
    if (allowedRoles.length > 0 && !allowedRoles.includes('EMPLOYEE')) {
      console.log('ProtectedRoute: EMPLOYEE user trying to access restricted route, redirecting to /attendance-scan');
      console.log('Allowed roles for this route:', allowedRoles);
      return <Navigate to="/attendance-scan" replace />;
    }
  }

  // If no specific roles required, just need authentication
  if (allowedRoles.length === 0) {
    return children;
  }

  // Check if user has required role
  const hasRequiredRole = cleanRoles.some(cleanRole => allowedRoles.includes(cleanRole));

  if (!hasRequiredRole) {
    console.log('ProtectedRoute: User does not have required role. Required:', allowedRoles, 'User has:', cleanRoles);
    
    // If EMPLOYEE user trying to access non-EMPLOYEE route, redirect to attendance
    if (cleanRoles.includes('EMPLOYEE')) {
      console.log('ProtectedRoute: EMPLOYEE user denied access, redirecting to /attendance-scan');
      return <Navigate to="/attendance-scan" replace />;
    }
    
    // Otherwise redirect to login
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;

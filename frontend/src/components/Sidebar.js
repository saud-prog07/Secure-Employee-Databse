import React, { useState, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  FaTachometerAlt,
  FaUsers,
  FaClipboardList,
  FaMoneyBillWave,
  FaUserTie,
  FaHistory,
  FaUser,
  FaSignOutAlt,
  FaCalendarAlt
} from 'react-icons/fa';
import './Sidebar.css';

const Sidebar = () => {
  const [expanded, setExpanded] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const username = localStorage.getItem('username') || 'User';
  
  // Parse roles from localStorage
  const rolesString = localStorage.getItem('roles');
  let userRoles = [];
  try {
    userRoles = rolesString ? JSON.parse(rolesString) : [];
  } catch (e) {
    console.error('Error parsing roles:', e);
    userRoles = [];
  }
  
  // Convert backend role format (ROLE_ADMIN) to simple format (ADMIN)
  const simplifiedRoles = userRoles.map(role => role.replace('ROLE_', ''));

  const menuItems = [
    {
      icon: FaTachometerAlt,
      label: 'Dashboard',
      path: '/dashboard',
      roles: ['ADMIN', 'HR']
    },
    {
      icon: FaUsers,
      label: 'Employees',
      path: '/add',
      roles: ['ADMIN']
    },
    {
      icon: FaClipboardList,
      label: 'Attendance',
      path: '/attendance-scan',
      roles: ['EMPLOYEE', 'ADMIN', 'HR']
    },
    {
      icon: FaCalendarAlt,
      label: 'Workday',
      path: '/workday',
      roles: ['ADMIN', 'HR']
    },
    {
      icon: FaMoneyBillWave,
      label: 'Payroll',
      path: '/payroll/list',
      roles: ['ADMIN', 'HR']
    },
    {
      icon: FaUserTie,
      label: 'Manage Users',
      path: '/admin/users',
      roles: ['ADMIN']
    },
    {
      icon: FaHistory,
      label: 'Audit Logs',
      path: '/admin/audit',
      roles: ['ADMIN', 'HR']
    },
    {
      icon: FaUser,
      label: 'Profile',
      path: '/profile',
      roles: ['ADMIN', 'HR', 'EMPLOYEE']
    }
  ];

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('username');
    navigate('/login', { replace: true });
  };

  const handleNavigation = (path) => {
    navigate(path);
    setExpanded(false);
  };

  // Check if current route matches menu item path
  const isActive = (itemPath) => {
    return location.pathname === itemPath || location.pathname.startsWith(itemPath + '/');
  };

  const canAccessItem = (roles) => {
    if (!roles || roles.length === 0) return true;
    
    // Check if user has any of the required roles
    return roles.some(requiredRole => {
      return simplifiedRoles.includes(requiredRole);
    });
  };

  // Filter menu items based on user role using useMemo
  const visibleMenuItems = useMemo(() => {
    return menuItems.filter(item => canAccessItem(item.roles));
  }, [simplifiedRoles]);

  return (
    <div
      className={`sidebar ${expanded ? 'expanded' : 'collapsed'}`}
      onMouseEnter={() => setExpanded(true)}
      onMouseLeave={() => setExpanded(false)}
    >
      {/* Sidebar Header */}
      <div className="sidebar-header">
        <div className="logo-icon">
          <FaTachometerAlt size={24} color="#fff" />
        </div>
        {expanded && <span className="logo-text">EMS</span>}
      </div>

      {/* User Info */}
      {expanded && (
        <div className="user-info">
          <div className="user-avatar">
            {username.charAt(0).toUpperCase()}
          </div>
          <div className="user-details">
            <p className="username">{username}</p>
            <p className="user-role">
              {simplifiedRoles.length > 0 ? simplifiedRoles[0] : 'EMPLOYEE'}
            </p>
          </div>
        </div>
      )}

      {/* Menu Items */}
      <nav className="sidebar-menu">
        {visibleMenuItems.map((item, index) => {
          const Icon = item.icon;
          const active = isActive(item.path);
          return (
            <div
              key={index}
              className={`menu-item ${active ? 'active' : ''}`}
              onClick={() => handleNavigation(item.path)}
              title={item.label}
              role="menuitem"
            >
              <div className="menu-icon">
                <Icon size={20} />
              </div>
              {expanded && (
                <span className="menu-label">{item.label}</span>
              )}
            </div>
          );
        })}
      </nav>

      {/* Logout Button */}
      <div className="sidebar-footer">
        <div
          className="logout-btn"
          onClick={handleLogout}
          title="Logout"
        >
          <FaSignOutAlt size={20} />
          {expanded && <span className="logout-text">Logout</span>}
        </div>
      </div>
    </div>
  );
};

export default Sidebar;

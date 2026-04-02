import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/EmployeeWorkday.css';

const EmployeeWorkday = () => {
  const [employees, setEmployees] = useState([]);
  const [workdayStats, setWorkdayStats] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [filter, setFilter] = useState('');

  const token = localStorage.getItem('token');

  useEffect(() => {
    fetchEmployees();
    fetchCurrentYear();
  }, []);

  const fetchCurrentYear = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/workday/current-year', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSelectedYear(response.data.currentYear);
    } catch (err) {
      console.log('Current year fetch info:', err.message);
    }
  };

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      setError('');
      
      // Call the /api/workday/all endpoint which returns all employees with their stats
      const response = await axios.get(
        `http://localhost:8080/api/workday/all?year=${selectedYear}`,
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );
      
      console.log("Workday stats response:", response.data);
      
      let stats = [];
      
      // Handle different response structures
      if (response.data && response.data.data) {
        // If wrapped in ApiResponse
        stats = Array.isArray(response.data.data) ? response.data.data : (response.data.data.content || []);
      } else if (Array.isArray(response.data)) {
        // If directly an array
        stats = response.data;
      }
      
      console.log("Processed stats:", stats);
      
      if (stats && stats.length > 0) {
        setWorkdayStats(stats);
        setEmployees(stats); // Set employees to the full stats objects
        setError('');
      } else {
        setWorkdayStats([]);
        setEmployees([]);
        setError('No employees found');
      }
    } catch (err) {
      console.error('Error fetching workday stats:', err);
      setError('Failed to load employee workday statistics');
      setWorkdayStats([]);
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchWorkdayStats = async (employeeId) => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/workday/stats/${employeeId}?year=${selectedYear}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      if (response.data && response.data.data) {
        setWorkdayStats(prev => ({
          ...prev,
          [employeeId]: response.data.data
        }));
      }
    } catch (err) {
      console.error(`Failed to fetch workday stats for employee ${employeeId}:`, err);
    }
  };

  const handleYearChange = (e) => {
    const year = parseInt(e.target.value);
    setSelectedYear(year);
    setWorkdayStats([]); // Clear cache
    
    // Re-fetch all employee stats for new year
    setTimeout(() => {
      fetchEmployees();
    }, 100);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'EXCELLENT': return '#10b981';
      case 'GOOD': return '#3b82f6';
      case 'AVERAGE': return '#f59e0b';
      case 'POOR': return '#ef4444';
      default: return '#6b7280';
    }
  };

  const filteredEmployees = (employees && Array.isArray(employees) ? employees : []).filter(emp => {
    // Each employee object now contains the full stats
    if (filter === '') return true;
    if (filter === 'EXCELLENT' && emp?.status === 'EXCELLENT') return true;
    if (filter === 'GOOD' && emp?.status === 'GOOD') return true;
    if (filter === 'AVERAGE' && emp?.status === 'AVERAGE') return true;
    if (filter === 'POOR' && emp?.status === 'POOR') return true;
    return false;
  });

  if (loading && employees.length === 0) {
    return <div className="workday-container"><p>Loading...</p></div>;
  }

  return (
    <div className="workday-container">
      <div className="workday-header">
        <h1>Employee Workday Statistics</h1>
        <p>Track total workdays and attendance performance for {selectedYear}</p>
      </div>

      {error && (
        <div className="error-box">
          <p>{error}</p>
          <button onClick={() => setError('')}>×</button>
        </div>
      )}

      {success && (
        <div className="success-box">
          <p>{success}</p>
          <button onClick={() => setSuccess('')}>×</button>
        </div>
      )}

      <div className="workday-controls">
        <div className="control-group">
          <label>Year:</label>
          <select value={selectedYear} onChange={handleYearChange}>
            <option value={2024}>2024</option>
            <option value={2025}>2025</option>
            <option value={2026}>2026</option>
            <option value={2027}>2027</option>
          </select>
        </div>

        <div className="control-group">
          <label>Filter by Status:</label>
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="">All Status</option>
            <option value="EXCELLENT">Excellent (90%+)</option>
            <option value="GOOD">Good (75-89%)</option>
            <option value="AVERAGE">Average (60-74%)</option>
            <option value="POOR">Poor (&lt;60%)</option>
          </select>
        </div>
      </div>

      <div className="workday-stats-grid">
        {filteredEmployees.length > 0 ? (
          filteredEmployees.map(emp => {
            return (
              <div key={emp.employeeId} className="workday-card">
                <div className="card-header">
                  <div className="employee-info">
                    <h3>{emp.employeeName}</h3>
                    <p className="email">{emp.employeeId}</p>
                  </div>
                  <div className="status-badge" style={{ backgroundColor: getStatusColor(emp.status) }}>
                    {emp.status}
                  </div>
                </div>

                <div className="card-content">
                  <div className="stats-row">
                    <div className="stat-item">
                      <span className="stat-label">Total Workdays</span>
                      <span className="stat-value">{emp.totalWorkdaysInYear}</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Present Days</span>
                      <span className="stat-value present">{emp.presentDays}</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Absent Days</span>
                      <span className="stat-value absent">{emp.absentDays}</span>
                    </div>
                  </div>

                  <div className="percentage-section">
                    <div className="percentage-display">
                      <span className="percentage-value">{emp.attendancePercentage}%</span>
                      <span className="percentage-label">Attendance</span>
                    </div>
                    <div className="progress-bar-container">
                      <div 
                        className="progress-bar" 
                        style={{
                          width: `${emp.attendancePercentage}%`,
                          backgroundColor: getStatusColor(emp.status)
                        }}
                      />
                    </div>
                  </div>

                  <div className="additional-info">
                    <div className="info-item">
                      <span>Weekend Days (Year):</span>
                      <span>{emp.weekendDays}</span>
                    </div>
                    <div className="info-item">
                      <span>Public Holidays:</span>
                      <span>{emp.holidayDays}</span>
                    </div>
                  </div>
                </div>
              </div>
            );
          })
        ) : (
          <div className="no-data">
            <p>No employees found matching your filter</p>
            {filter && (
              <button onClick={() => setFilter('')} className="clear-filter-btn">
                Clear Filter
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default EmployeeWorkday;

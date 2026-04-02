# Setup & Developer Guide - Complete Reference

This guide contains all detailed setup, API documentation, testing procedures, and troubleshooting for the Secure Employee Management System.

---

## Table of Contents
1. [Installation & Setup](#installation--setup)
2. [Database Configuration](#database-configuration)
3. [Detailed API Reference](#detailed-api-reference)
4. [Feature Implementation Details](#feature-implementation-details)
5. [Testing Procedures](#testing-procedures)
6. [Configuration Guide](#configuration-guide)
7. [Troubleshooting & FAQ](#troubleshooting--faq)

---

## Installation & Setup
---

### Prerequisites
- Java 17+ (JDK)
- Node.js 16+ with npm
- MySQL 8.0 server
- Docker & Docker Compose (optional, for containerization)
- Git (for version control)

### Step 1: Clone/Download Project
```bash
cd /path/to/projects
# If from git:
git clone <repository-url>
cd Secure-Employee-Management-System-API
```

### Step 2: Start Database (Docker Recommended)
```bash
# Using Docker Compose (creates MySQL container)
docker compose up -d

# Verify container is running
docker ps  # Should show MySQL container on port 3307

# OR manually start MySQL
# Ensure MySQL is running on localhost:3306 (or configured in application.properties)
```

### Step 3: Run Database Migrations
Execute these SQL scripts **in order** in your MySQL database:

```bash
# Connect to MySQL
mysql -u root -p employee_management_db < db-migration-2fa.sql
mysql -u root -p employee_management_db < db-migration-approval-workflow.sql
mysql -u root -p employee_management_db < db-migration-payroll.sql
mysql -u root -p employee_management_db < db-migration-attendance.sql
```

Or execute each file via MySQL Workbench/CLI:
```sql
SOURCE /path/to/db-migration-2fa.sql;
SOURCE /path/to/db-migration-approval-workflow.sql;
SOURCE /path/to/db-migration-payroll.sql;
SOURCE /path/to/db-migration-attendance.sql;
```

### Step 4: Build Backend
```bash
# Build project with Maven
mvn clean install -DskipTests

# Output: target/employee-management-system-0.0.1-SNAPSHOT.jar
```

### Step 5: Start Backend Server
```bash
# Option A: Using Maven Spring Boot plugin
mvn spring-boot:run

# Option B: Using JAR directly
java -jar target/employee-management-system-0.0.1-SNAPSHOT.jar

# Server starts on http://localhost:8080
# Swagger docs: http://localhost:8080/swagger-ui.html
```

### Step 6: Setup Frontend
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Opens http://localhost:3000 in browser
```

### Step 7: Verify Installation
1. Open http://localhost:3000 in browser
2. Login with credentials:
   - **ADMIN**: username: `admin`, password: `admin`
   - **HR**: username: `hr`, password: `hr`
3. You should see the Dashboard

---

## Database Configuration
---
**File**: `src/main/resources/application.properties`

```properties
# Database Connection
spring.datasource.url=jdbc:mysql://localhost:3307/employee_management_db
spring.datasource.username=root
spring.datasource.password=root123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate/JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
```

### Database Schema Overview

#### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20),  -- ADMIN, HR, USER
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by VARCHAR(255),
    approved_at TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### Employees Table
```sql
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    department VARCHAR(100),
    salary DECIMAL(10, 2),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by VARCHAR(255),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

#### Payroll Table
```sql
CREATE TABLE payroll (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    base_salary DECIMAL(10, 2) NOT NULL,
    bonus DECIMAL(10, 2) DEFAULT 0.00,
    deductions DECIMAL(10, 2) DEFAULT 0.00,
    final_salary DECIMAL(10, 2) NOT NULL,
    month VARCHAR(7) NOT NULL,  -- YYYY-MM
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    UNIQUE KEY uk_employee_month (employee_id, month)
);
```

#### Attendance Table
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    login_time DATETIME NOT NULL,
    status ENUM('PRESENT', 'ABSENT') NOT NULL DEFAULT 'PRESENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY uk_employee_date (employee_id, date),
    INDEX idx_employee_date (employee_id, date),
    INDEX idx_date (date)
);
```

#### Audit Logs Table
```sql
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(50),  -- CREATE, UPDATE, DELETE, APPROVE, REJECT, LOGIN
    entity_type VARCHAR(50),  -- USER, EMPLOYEE, PAYROLL
    entity_id BIGINT,
    description TEXT,
    performed_by VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action)
);
```

---

## Detailed API Reference
---

#### POST /api/auth/register
Register a new user (HR or ADMIN).

**Request**:
```json
{
  "username": "john.doe",
  "password": "secure_password_123",
  "email": "john@company.com",
  "role": "HR"
}
```

**Response (201 Created)**:
```json
{
  "status": "success",
  "message": "User registered successfully. Awaiting admin approval.",
  "data": {
    "id": 1,
    "username": "john.doe",
    "email": "john@company.com",
    "role": "HR",
    "status": "PENDING"
  }
}
```

**Error (400)**:
```json
{
  "status": "error",
  "message": "Username already exists"
}
```

**Triggers**: Automatic registration email sent.

---

#### POST /api/auth/login
Login with username and password.

**Request**:
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Response if 2FA disabled (200 OK)**:
```json
{
  "status": "SUCCESS",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "roles": ["ROLE_ADMIN"],
  "twoFactorEnabled": false
}
```

**Response if 2FA enabled (200 OK)**:
```json
{
  "status": "OTP_REQUIRED",
  "token": null,
  "username": "admin",
  "roles": ["ROLE_ADMIN"],
  "twoFactorEnabled": true
}
```

**Error (401)**:
```json
{
  "status": "error",
  "message": "Invalid username or password"
}
```

---

#### POST /api/auth/verify-otp
Verify OTP code for 2FA login.

**Request**:
```json
{
  "username": "admin",
  "otp": "123456"
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "OTP verified successfully",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

**Error (400)**:
```json
{
  "status": "error",
  "message": "Invalid or expired OTP"
}
```

---

#### POST /api/auth/2fa/setup
Generate QR code for 2FA setup.

**Response (200 OK)**:
```json
{
  "status": "success",
  "data": {
    "secret": "JBSWY3DPEBLW64TMMQ======",
    "qrBarcode": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

**Steps**:
1. Call this endpoint
2. Scan QR code with authenticator app (Google Authenticator, Authy, etc.)
3. Get 6-digit code from app
4. Call `/api/auth/2fa/confirm` with the code

---

#### POST /api/auth/2fa/confirm
Enable 2FA for current user.

**Request**:
```json
{
  "otp": "123456"
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Two-factor authentication enabled successfully"
}
```

---

#### POST /api/auth/2fa/disable
Disable 2FA for current user.

**Request**:
```json
{
  "password": "your_password"
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Two-factor authentication disabled"
}
```

---

### User Management Endpoints

#### GET /api/v1/users
Get all system users with optional filtering.

**Query Parameters**:
- `status`: PENDING, APPROVED, REJECTED
- `page`: Page number (0-indexed)
- `size`: Items per page (default: 20)

**Request**: `GET /api/v1/users?status=PENDING&page=0&size=10`

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "username": "john.doe",
      "email": "john@company.com",
      "role": "HR",
      "status": "PENDING",
      "approvedBy": null,
      "twoFactorEnabled": false
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0
}
```

**Authorization**: ADMIN only

---

#### PUT /api/v1/users/{id}/approve
Approve a pending user registration.

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "User approved successfully",
  "data": {
    "id": 1,
    "username": "john.doe",
    "status": "APPROVED",
    "approvedBy": "admin",
    "approvedAt": "2026-04-01T10:30:00"
  }
}
```

**Triggers**: Approval email sent to user.

---

#### PUT /api/v1/users/{id}/disapprove
Revoke approval for an approved user.

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "User disapproved successfully",
  "data": {
    "id": 1,
    "status": "REJECTED"
  }
}
```

---

#### DELETE /api/v1/users/{id}
Delete a user (permanent).

**Query Parameters**:
- `permanent`: true (for hard delete) or false (soft delete)

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "User deleted successfully"
}
```

**Note**: ADMIN cannot delete their own account (safety check).

---

### Employee Management Endpoints

#### POST /api/v1/employees
Create a new employee record (HR role).

**Request**:
```json
{
  "name": "John Doe",
  "email": "john@company.com",
  "department": "Engineering",
  "salary": 75000
}
```

**Response (201 Created)**:
```json
{
  "status": "success",
  "message": "Employee created successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@company.com",
    "department": "Engineering",
    "salary": 75000,
    "status": "PENDING",
    "createdAt": "2026-04-01T10:30:00"
  }
}
```

**Authorization**: HR, ADMIN

---

#### GET /api/v1/employees
Get list of employees with filtering and pagination.

**Query Parameters**:
- `status`: PENDING, APPROVED, REJECTED
- `department`: Filter by department
- `search`: Search by name or email
- `page`: Page number
- `size`: Items per page

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@company.com",
      "department": "Engineering",
      "salary": 75000,
      "status": "APPROVED",
      "approvedBy": "admin",
      "approvedAt": "2026-04-01T11:00:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

---

#### PUT /api/v1/employees/{id}
Edit employee information.

**Request**:
```json
{
  "name": "John Doe",
  "email": "john.updated@company.com",
  "department": "Management",
  "salary": 80000
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Employee updated successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john.updated@company.com",
    "department": "Management",
    "salary": 80000,
    "status": "APPROVED"
  }
}
```

---

#### PUT /api/v1/employees/{id}/approve
Approve a pending employee.

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Employee approved successfully. Notification email sent.",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "approvedBy": "admin",
    "approvedAt": "2026-04-01T11:30:00"
  }
}
```

**Authorization**: ADMIN only
**Triggers**: Approval email sent to employee

---

#### PUT /api/v1/employees/{id}/reject
Reject a pending or approved employee.

**Request**:
```json
{
  "rejectionReason": "Does not meet requirements"
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Employee rejected successfully. Notification email sent.",
  "data": {
    "id": 1,
    "status": "REJECTED",
    "approvedBy": "admin"
  }
}
```

**Authorization**: ADMIN only
**Triggers**: Rejection email sent to employee

---

#### DELETE /api/v1/employees/{id}
Delete an employee (soft delete by default).

**Query Parameters**:
- `permanent`: true for hard delete, false for soft delete (default)

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Employee deleted successfully"
}
```

**Authorization**: ADMIN

---

#### GET /api/v1/employees/approvals/pending
Get pending employee approvals (ADMIN dashboard).

**Query Parameters**:
- `page`: Page number
- `size`: Items per page

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "name": "Jane Smith",
      "email": "jane@company.com",
      "department": "HR",
      "status": "PENDING",
      "createdAt": "2026-04-01T09:00:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

---

#### GET /api/v1/employees/approvals/pending/count
Get count of pending approvals (for dashboard badge).

**Response (200 OK)**:
```json
{
  "status": "success",
  "data": {
    "pendingCount": 5
  }
}
```

---

#### GET /api/v1/employees/{id}/qr
Generate and retrieve QR code for employee.

**Response (200 OK)**: Returns QR code image (PNG format)
- Content-Type: image/png
- Binary image data

**Example Usage**:
```bash
# Download QR code and save as image
curl -X GET http://localhost:8080/api/v1/employees/1/qr \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output employee_qr_1.png

# Display QR in browser/app
<img src="/api/v1/employees/1/qr" alt="Employee QR Code" />
```

**Features**:
- Unique QR per employee (encodes employee ID)
- High resolution (200x200 pixels)
- Works with standard QR scanners
- Useful for attendance kiosks

**Authorization**: Authenticated users (any role)

---

### Payroll Management Endpoints

#### POST /api/payroll/generate
Generate payroll for an employee.

**Request**:
```json
{
  "employeeId": 1,
  "baseSalary": 50000,
  "bonus": 5000,
  "deductions": 2500,
  "month": "2026-04"
}
```

**Response (201 Created)**:
```json
{
  "status": "success",
  "message": "Payroll generated successfully",
  "data": {
    "id": 1,
    "employeeId": 1,
    "employeeName": "John Doe",
    "baseSalary": 50000,
    "bonus": 5000,
    "deductions": 2500,
    "finalSalary": 52500,
    "month": "2026-04",
    "createdAt": "2026-04-01T10:30:00"
  }
}
```

**Validation**:
- Month format: YYYY-MM
- No duplicate payroll for same employee/month
- All amounts must be positive

**Authorization**: ADMIN only

---

#### GET /api/payroll
Get all payroll records with filtering.

**Query Parameters**:
- `month`: Filter by month (YYYY-MM)
- `page`: Page number
- `size`: Items per page

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "employeeId": 1,
      "employeeName": "John Doe",
      "baseSalary": 50000,
      "bonus": 5000,
      "deductions": 2500,
      "finalSalary": 52500,
      "month": "2026-04"
    }
  ],
  "totalElements": 10,
  "totalPages": 1
}
```

**Authorization**: ADMIN, HR

---

#### GET /api/payroll/{id}
Get specific payroll record.

**Response (200 OK)**:
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "employeeId": 1,
    "employeeName": "John Doe",
    "baseSalary": 50000,
    "bonus": 5000,
    "deductions": 2500,
    "finalSalary": 52500,
    "month": "2026-04"
  }
}
```

---

#### GET /api/payroll/employee/{employeeId}
Get payroll history for an employee.

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "month": "2026-04",
      "baseSalary": 50000,
      "bonus": 5000,
      "deductions": 2500,
      "finalSalary": 52500
    },
    {
      "id": 2,
      "month": "2026-03",
      "baseSalary": 50000,
      "bonus": 3000,
      "deductions": 2000,
      "finalSalary": 51000
    }
  ]
}
```

---

#### PUT /api/payroll/{id}
Update payroll record.

**Request**:
```json
{
  "baseSalary": 50000,
  "bonus": 5500,
  "deductions": 2700
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "baseSalary": 50000,
    "bonus": 5500,
    "deductions": 2700,
    "finalSalary": 52800
  }
}
```

**Authorization**: ADMIN only

---

#### DELETE /api/payroll/{id}
Delete payroll record.

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Payroll deleted successfully"
}
```

**Authorization**: ADMIN only

---

### Notification Endpoints

#### POST /api/notifications/email/send
Send custom email (ADMIN only).

**Request**:
```json
{
  "to": "user@example.com",
  "subject": "Important Notice",
  "body": "<h1>Hello</h1><p>This is an important message</p>",
  "isHtml": true
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Email sent successfully"
}
```

---

#### POST /api/notifications/sms/send
Send SMS via Twilio (ADMIN only).

**Request**:
```json
{
  "phoneNumber": "+14155552671",
  "message": "Your OTP code is: 123456"
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "SMS sent successfully"
}
```

---

#### POST /api/notifications/test/email/{email}
Test email configuration.

**Example**: `POST /api/notifications/test/email/test@example.com`

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Test email sent successfully to test@example.com"
}
```

---

### Attendance Endpoints (Public & Secured)

#### POST /api/attendance/scan
PUBLIC endpoint - Scan attendance with employee ID.

**Request**:
```json
{
  "employeeId": 1
}
```

**Response on First Login (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance scanned successfully",
  "data": {
    "employeeId": 1,
    "employeeName": "John Doe",
    "loginTime": "2026-04-01T09:30:15",
    "date": "2026-04-01",
    "message": "Logged in at 09:30 AM"
  }
}
```

**Response on Duplicate Scan (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance scanned successfully",
  "data": {
    "employeeId": 1,
    "employeeName": "John Doe",
    "loginTime": "2026-04-01T09:30:15",
    "message": "Already logged in at 09:30 AM"
  }
}
```

**Error (404)**:
```json
{
  "success": false,
  "message": "Employee not found with ID: 999",
  "data": null
}
```

**Features**:
- Public access (no authentication required)
- One login per calendar day
- Automatic duplicate prevention
- Real-time feedback with employee name
- Audit logging of all scans

---

#### GET /api/attendance/status/{employeeId}
PUBLIC endpoint - Check if employee is logged in today.

**Example**: `GET /api/attendance/status/1`

**Response if Logged In (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance status retrieved",
  "data": {
    "employeeId": 1,
    "employeeName": "John Doe",
    "loginTime": "2026-04-01T09:30:15",
    "date": "2026-04-01",
    "message": "Logged in at 09:30 AM"
  }
}
```

**Response if NOT Logged In (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance status retrieved",
  "data": {
    "employeeId": 1,
    "employeeName": "John Doe",
    "loginTime": null,
    "date": "2026-04-01",
    "message": "NOT LOGGED IN"
  }
}
```

---

#### GET /api/attendance/history/{employeeId}
Get attendance history for an employee.

**Authorization**: ADMIN, HR

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Items per page (default: 20)

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance history retrieved",
  "data": [
    {
      "id": 1,
      "employeeId": 1,
      "date": "2026-04-01",
      "loginTime": "2026-04-01T09:30:15",
      "status": "PRESENT",
      "createdAt": "2026-04-01T09:30:15"
    },
    {
      "id": 2,
      "employeeId": 1,
      "date": "2026-03-31",
      "loginTime": "2026-03-31T09:15:00",
      "status": "PRESENT",
      "createdAt": "2026-03-31T09:15:00"
    }
  ]
}
```

---

#### GET /api/attendance/today
Get today's attendance records for all employees.

**Authorization**: ADMIN, HR

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Today's attendance retrieved",
  "data": [
    {
      "id": 1,
      "employeeId": 1,
      "date": "2026-04-01",
      "loginTime": "2026-04-01T09:30:15",
      "status": "PRESENT"
    },
    {
      "id": 2,
      "employeeId": 2,
      "date": "2026-04-01",
      "loginTime": "2026-04-01T09:45:00",
      "status": "PRESENT"
    }
  ]
}
```

---

#### GET /api/attendance/range/{employeeId}
Get attendance for a date range.

**Query Parameters**:
- `startDate`: Start date (YYYY-MM-DD) - Required
- `endDate`: End date (YYYY-MM-DD) - Required

**Example**: `GET /api/attendance/range/1?startDate=2026-03-01&endDate=2026-04-01`

**Authorization**: ADMIN, HR

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance range retrieved",
  "data": [
    {
      "id": 1,
      "employeeId": 1,
      "date": "2026-04-01",
      "loginTime": "2026-04-01T09:30:15",
      "status": "PRESENT"
    }
  ]
}
```

---

#### GET /api/attendance/summary/{employeeId}
Get overall attendance summary and analytics for an employee.

**Authorization**: ADMIN, HR

**Example**: `GET /api/attendance/summary/1`

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance summary retrieved",
  "data": {
    "employeeId": 1,
    "employeeName": "John Doe",
    "totalDays": 20,
    "presentDays": 18,
    "absentDays": 2,
    "attendancePercentage": 90.0,
    "status": "GOOD"
  }
}
```

**Field Descriptions**:
- `employeeId`: Employee unique identifier
- `employeeName`: Full name of the employee
- `totalDays`: Total attendance records (days tracked)
- `presentDays`: Count of days marked as PRESENT
- `absentDays`: Count of days absent (totalDays - presentDays)
- `attendancePercentage`: Percentage of days present (presentDays / totalDays * 100)
- `status`: Performance status
  - GOOD: >= 75% attendance
  - AVERAGE: 60-74% attendance 
  - POOR: < 60% attendance

**Use Cases**:
- Employee performance evaluation
- Promotion decision logic
- Identifying attendance problems
- HR reporting and compliance

---

#### GET /api/attendance/summary/{employeeId}/month/{YYYY-MM}
Get monthly attendance summary for an employee.

**Authorization**: ADMIN, HR

**Example**: `GET /api/attendance/summary/1/month/2026-04`

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Monthly attendance summary retrieved",
  "data": {
    "employeeId": 1,
    "employeeName": "John Doe",
    "totalDays": 22,
    "presentDays": 20,
    "absentDays": 2,
    "attendancePercentage": 90.91,
    "status": "GOOD"
  }
}
```

**Parameters**:
- `employeeId`: Employee ID (path parameter)
- `YYYY-MM`: Year and Month in ISO format (e.g., 2026-04)

**Note**: Calculates attendance only for the specified month.

---

### Audit Log Endpoints

#### GET /api/admin/audit
Get all audit logs.

**Query Parameters**:
- `action`: CREATE, UPDATE, DELETE, APPROVE, REJECT, LOGIN
- `entityType`: USER, EMPLOYEE, PAYROLL
- `performedBy`: Username filter
- `page`: Page number
- `size`: Items per page

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "action": "LOGIN",
      "entityType": "USER",
      "entityId": 1,
      "description": "User login successful",
      "performedBy": "admin",
      "timestamp": "2026-04-01T10:30:00"
    },
    {
      "id": 2,
      "action": "APPROVE",
      "entityType": "EMPLOYEE",
      "entityId": 5,
      "description": "Employee approved",
      "performedBy": "admin",
      "timestamp": "2026-04-01T11:00:00"
    }
  ],
  "totalElements": 150,
  "totalPages": 8
}
```

**Authorization**: ADMIN only

---

### Profile Endpoints

#### GET /api/auth/profile
Get current user's profile.

**Response (200 OK)**:
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@company.com",
    "role": "ADMIN",
    "status": "APPROVED",
    "twoFactorEnabled": true,
    "createdAt": "2026-04-01T10:00:00"
  }
}
```

---

#### PUT /api/auth/change-password
Change password for logged-in user.

**Request**:
```json
{
  "oldPassword": "current_password",
  "newPassword": "new_secure_password",
  "confirmPassword": "new_secure_password"
}
```

**Response (200 OK)**:
```json
{
  "status": "success",
  "message": "Password changed successfully"
}
```

---

## Feature Implementation Details
--- Two-Factor Authentication (2FA)

**How It Works**:
1. User enables 2FA in profile settings
2. Backend generates QR code (TOTP secret)
3. User scans with authenticator app (Google Authenticator, etc.)
4. On next login:
   - Backend returns `status: OTP_REQUIRED`
   - Frontend redirects to OTP verification page
   - User enters 6-digit code from app
   - Backend validates code using TOTP algorithm
   - JWT token provided on successful verification

**Files**:
- `TwoFactorService.java` - Core 2FA logic
- `OtpVerification.js` - Frontend OTP page
- Database schema: `two_factor_enabled`, `two_factor_secret` columns

**Testing 2FA**:
```bash
# 1. Setup 2FA
curl -X POST http://localhost:8080/api/auth/2fa/setup \
  -H "Authorization: Bearer YOUR_TOKEN"

# 2. Scan QR code with authenticator app
# 3. Get 6-digit code from app

# 4. Confirm 2FA
curl -X POST http://localhost:8080/api/auth/2fa/confirm \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"otp": "123456"}'

# 5. Logout and try login - should ask for OTP
```

---

### Payroll Calculation

**Formula**:
```
Final Salary = Base Salary + Bonus - Deductions
```

**Example**:
```
Base Salary: $50,000
Bonus:       $5,000
Deductions:  $2,500
Final:       $52,500 (50,000 + 5,000 - 2,500)
```

**Calculation Logic**:
- Pre-calculated and stored in database
- Updated automatically on `@PrePersist` and `@PreUpdate`
- Immutable once created (require update to change)

---

### Employee Approval Workflow

**States**:
1. **PENDING** - Created by HR, awaiting admin approval
2. **APPROVED** - Approved by admin (tracks who & when)
3. **REJECTED** - Rejected by admin (can be re-submitted)

**Flow**:
```
HR Creates Employee
    ↓
Employee Status: PENDING
    ↓
ADMIN Reviews
    ↓
    ├→ Approve → Status: APPROVED (Email sent)
    └→ Reject  → Status: REJECTED (Email sent)
```

---

### Email Notifications

**Automatic Triggers**:
1. **User Registration** - Welcome email sent
2. **User Approval** - Approval notification email
3. **User Rejection** - Rejection explanation email
4. **Employee Approval** - Employee notified of approval
5. **Employee Rejection** - Employee directed to HR

**Configuration** (`application.properties`):
```properties
# Gmail SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Twilio (optional SMS)
twilio.account-sid=ACxxx
twilio.auth-token=xxx
twilio.phone-number=+1234567890
```

**Testing Email**:
```bash
# Test configuration
curl -X POST http://localhost:8080/api/notifications/test/email/test@gmail.com \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Should receive test email within 30 seconds
```

---

### Audit Logging

**Logged Actions**:
- **LOGIN** - Successful login attempts
- **REGISTER** - New user registration
- **CREATE** - Create employees/users
- **UPDATE** - Modify employees/users
- **DELETE** - Delete employees/users
- **APPROVE** - Approve pending records
- **REJECT** - Reject records
- **2FA_SETUP** - 2FA  enabled
- **2FA_VERIFY** - OTP verification
- **EMAIL_SENT** - Notifications sent

**Audit Query Example**:
```bash
# Get all approvals by admin
curl -X GET "http://localhost:8080/api/admin/audit?action=APPROVE&performedBy=admin" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## Testing Procedures
---

### Unit Testing

**Run unit tests**:
```bash
mvn test
```

**Test files location**:
```
src/test/java/com/example/employee/service/
src/test/java/com/example/employee/controller/
```

### Integration Testing

**Test with Postman**:
1. Import Postman collection from project
2. Set base URL to `http://localhost:8080`
3. Set Auth token in Postman environment
4. Run test suite

### Manual Testing Scenarios

#### Scenario 1: User Registration & Approval
```bash
# Step 1: Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@company.com",
    "role": "HR"
  }'

# Step 2: Login as ADMIN
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'

# Step 3: Approve new user
curl -X PUT http://localhost:8080/api/v1/users/2/approve \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Step 4: Login as new user (should succeed)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123"
  }'
```

#### Scenario 2: Employee Creation & Approval
```bash
# Step 1: Create employee (as HR)
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Authorization: Bearer HR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@company.com",
    "department": "Engineering",
    "salary": 75000
  }'

# Step 2: Check status (should be PENDING)
curl -X GET http://localhost:8080/api/v1/employees \
  -H "Authorization: Bearer HR_TOKEN"

# Step 3: Approve (as ADMIN)
curl -X PUT http://localhost:8080/api/v1/employees/1/approve \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Step 4: Check status (should be APPROVED)
```

#### Scenario 3: Payroll Generation
```bash
# Step 1: Create payroll
curl -X POST http://localhost:8080/api/payroll/generate \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 1,
    "baseSalary": 50000,
    "bonus": 5000,
    "deductions": 2500,
    "month": "2026-04"
  }'

# Expected final salary: 52,500 (50,000 + 5,000 - 2,500)

# Step 2: Get payroll list
curl -X GET http://localhost:8080/api/payroll \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Step 3: Get employee payroll history
curl -X GET http://localhost:8080/api/payroll/employee/1 \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

#### Scenario 4: 2FA Setup & Login
```bash
# Step 1: Generate QR code
curl -X POST http://localhost:8080/api/auth/2fa/setup \
  -H "Authorization: Bearer USER_TOKEN"

# Step 2: Scan QR code with authenticator app

# Step 3: Get 6-digit code and confirm
curl -X POST http://localhost:8080/api/auth/2fa/confirm \
  -H "Authorization: Bearer USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"otp": "123456"}'

# Step 4: Logout and login again
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'

# Response should have: "status": "OTP_REQUIRED"

# Step 5: Verify OTP
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "otp": "123456"  # from authenticator app
  }'

# Should receive JWT token
```

### Frontend Testing

1. **Login Page**
   - Test normal login
   - Test login with 2FA (should redirect to OTP page)
   - Test invalid credentials

2. **Employee Management**
   - Create employee (HR role)
   - Check if PENDING appears in dashboard
   - Approve as admin
   - Check if status changes to APPROVED

3. **Payroll**
   - Generate payroll for employee
   - Verify calculation: Final = Base + Bonus - Deductions
   - Check payroll list

4. **User Management**
   - Register new user
   - Approve as admin
   - Try login (should succeed)

5. **Attendance Scanning**
   - Navigate to `/attendance` (public, no login required)
   - Enter employee ID (e.g., 1)
   - Click "Scan Attendance" → Should see "Logged in at [time]"
   - Try scanning again → Should see "Already logged in at [time]"
   - Check Status → Should show login time
   - (Admin) View today's attendance: `GET /api/attendance/today`
   - (Admin) View employee history: `GET /api/attendance/history/1`

#### Scenario 5: QR Code Generation & Display
```bash
# Step 1: Login to get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'

# Step 2: Generate QR code for employee
curl -X GET http://localhost:8080/api/v1/employees/1/qr \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output employee_qr_1.png

# Step 3: View QR in browser
# Navigate to Employee Profile or Dashboard
# QR code displays with employee details
# Click "Download QR" button to get image

# Step 4: Verify QR
# Each employee has unique QR code
# QR encodes employee ID
# Can be scanned with any QR code reader
# Useful for attendance kiosks
```

**Frontend Testing**:
   - Login to dashboard
   - View any employee profile → QR code displays
   - Click "Download QR" → Image saves to device
   - View Dashboard → All employees show their QR codes
   - QR codes are unique per employee
   - Scan QR with phone → Shows employee ID

#### Scenario 6: Attendance Analytics & Reporting
```bash
# Step 1: Scan attendance multiple times
curl -X POST http://localhost:8080/api/attendance/scan \
  -H "Content-Type: application/json" \
  -d '{"employeeId": 1}'

# Step 2: Get overall attendance summary
curl -X GET http://localhost:8080/api/attendance/summary/1 \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Response:
{
  "success": true,
  "message": "Attendance summary retrieved",
  "data": {
    "employeeId": 1,
    "employeeName": "John Doe",
    "totalDays": 20,
    "presentDays": 18,
    "absentDays": 2,
    "attendancePercentage": 90.0,
    "status": "GOOD"
  }
}

# Step 3: Get monthly attendance summary
curl -X GET http://localhost:8080/api/attendance/summary/1/month/2026-04 \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Step 4: Frontend verification
# Login to dashboard
# Open employee profile/edit page
# Attendance Analytics section shows:
#   - Percentage: 90.0%
#   - Status badge: GOOD
#   - Present days: 18
#   - Absent days: 2
#   - Total days: 20
#   - Progress bar visualization

# Expected colors:
# GOOD (>=75%): Green progress bar
# AVERAGE (60-74%): Yellow/Orange progress bar
# POOR (<60%): Red progress bar
```

**Key Features to Verify**:
- Attendance percentage calculated correctly
- Status assigned based on percentage threshold
- Present/absent counts match total
- Frontend displays analytics without login (if admin views it)
- Color-coded status badges
- Progress bar visualization
- Useful for promotion decisions and performance reviews

---

## Configuration Guide
---

**File**: `src/main/resources/application.properties`

```properties
# ============= SERVER SETTINGS =============
server.port=8080
server.servlet.context-path=/
spring.application.name=employee-management-system

# ============= DATABASE SETTINGS =============
spring.datasource.url=jdbc:mysql://localhost:3307/employee_management_db
spring.datasource.username=root
spring.datasource.password=root123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ============= JPA/HIBERNATE SETTINGS =============
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# ============= JWT SETTINGS =============
jwt.secret=${JWT_SECRET:your_secret_key_change_in_production_min_64_chars_long}
jwt.expiration=86400000  # 24 hours in milliseconds

# ============= EMAIL SETTINGS (Gmail SMTP) =============
spring.mail.host=${EMAIL_HOST:smtp.gmail.com}
spring.mail.port=${EMAIL_PORT:587}
spring.mail.username=${EMAIL_Username:your-email@gmail.com}
spring.mail.password=${EMAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

app.mail.from=${APP_MAIL_FROM:noreply@employee-management.com}
app.mail.from-name=${APP_MAIL_FROM_NAME:Employee Management System}

# ============= TWILIO SMS SETTINGS (Optional) =============
twilio.account-sid=${TWILIO_ACCOUNT_SID:}
twilio.auth-token=${TWILIO_AUTH_TOKEN:}
twilio.phone-number=${TWILIO_PHONE_NUMBER:}

# ============= CORS SETTINGS =============
cors.allowed-origins=http://localhost:3000,http://localhost:3001
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
cors.allowed-headers=*
cors.allow-credentials=true

# ============= LOGGING SETTINGS =============
logging.level.root=INFO
logging.level.com.example.employee=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Environment Variables (For Production)

```bash
# JWT Secret (minimum 64 characters)
export JWT_SECRET="your-very-long-secret-key-minimum-64-characters-for-production-use"

# Email Configuration
export EMAIL_HOST="smtp.gmail.com"
export EMAIL_PORT="587"
export EMAIL_USERNAME="your-email@gmail.com"
export EMAIL_PASSWORD="your-app-password-16-chars"

# Twilio SMS (Optional)
export TWILIO_ACCOUNT_SID="ACxxxxxxxxxx"
export TWILIO_AUTH_TOKEN="auth_token_here"
export TWILIO_PHONE_NUMBER="+1234567890"

# Database
export DB_URL="jdbc:mysql://localhost:3307/employee_management_db"
export DB_USERNAME="root"
export DB_PASSWORD="root123"
```

### Frontend Environment Variables

**File**: `frontend/.env`

```env
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_API_TIMEOUT=5000
REACT_APP_ENABLE_DEMO_MODE=false
```

---

## Troubleshooting & FAQ
---

#### Issue 1: Cannot Connect to Database
**Error**: `java.sql.SQLException: Access denied for user 'root'@'localhost'`

**Solutions**:
```bash
# Check MySQL is running
docker ps  # Should show MySQL container
mysql -u root -p -h 127.0.0.1 -P 3307

# Update credentials in application.properties
spring.datasource.username=your_user
spring.datasource.password=your_password
```

#### Issue 2: Port Already in Use
**Error**: `Address already in use: bind`

**Solutions**:
```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess  # Windows

# Kill process or change port
server.port=8081  # In application.properties
```

#### Issue 3: JWT Token Expired Error
**Error**: `401 Unauthorized: Token has expired`

**Solution**:
- Login again to get new token
- Token expires after 24 hours (configurable in `jwt.expiration`)
- Clear localStorage if needed: `localStorage.clear()`

#### Issue 4: CORS Error in Frontend
**Error**: `Access to XMLHttpRequest has been blocked by CORS policy`

**Solutions**:
- Add frontend URL to `cors.allowed-origins` in application.properties
- Ensure backend CORS config is correct
- Check frontend API base URL matches backend

#### Issue 5: Email Not Sending
**Error**: `SMTPAuthenticationException: 535 5.7.8 Username and password not accepted`

**Solutions**:
```bash
# For Gmail:
# 1. Enable 2FA on Gmail account
# 2. Generate app password at https://myaccount.google.com/apppasswords
# 3. Use 16-character app password (without spaces)
spring.mail.password=xxxx xxxx xxxx xxxx

# Test configuration
curl -X POST http://localhost:8080/api/notifications/test/email/test@gmail.com
```

#### Issue 6: 2FA OTP Not Working
**Error**: `Invalid or expired OTP`

**Solutions**:
- Ensure device time is accurate (TOTP requires precise time sync)
- Use codes within 30-second window
- Regenerate secret if codes consistently fail
- Time zone must match server

#### Issue 7: Payroll Duplicate Month Error
**Error**: `Constraint violation: uk_employee_month`

**Solution**:
- Cannot create duplicate payroll for same employee/month
- Update existing payroll with PUT endpoint
- Delete old entry first if needed

#### Issue 8: Soft Delete Employee Still Shows
**Error**: Deleted employees appearing in list

**Solution**:
- Frontend filters out soft-deleted records
- Check `is_deleted` column in database
- Restore with `/api/v1/employees/{id}/restore` endpoint

### Performance Optimization Tips

1. **Database Indexing**
   - Indexes created on: status, created_at, employee_id, month
   - Add more indexes for frequently filtered columns

2. **API Pagination**
   - Always use pagination for large datasets
   - Default page size: 20 items
   - Adjust with `size` parameter

3. **Frontend Optimization**
   - Enable production build: `npm run build`
   - Minify CSS and JS
   - Cache API responses when appropriate

4. **Caching**
   - User roles cached after login
   - Employee list cached (invalidate on update)
   - Consider Redis for distributed caching

### FAQ

**Q: How long do JWT tokens last?**
A: By default, 24 hours. Change in `application.properties`: `jwt.expiration=86400000`

**Q: Can I use database other than MySQL?**
A: Yes, update Hibernate dialect in `application.properties` for PostgreSQL, Oracle, etc.

**Q: How do I reset admin password?**
A: Update directly in database: `UPDATE users SET password='$2a$12$...' WHERE username='admin'`

**Q: Can I disable 2FA?**
A: Yes, per-user in settings or disable completely by removing 2FA endpoints.

**Q: How to backup database?**
A: `mysqldump -u root -p employee_management_db > backup.sql`

**Q: Can I export audit logs?**
A: Currently view via UI, implement CSV export in future enhancement.

---

## Additional Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **React Docs**: https://react.dev
- **JWT**: https://jwt.io
- **Swagger/OpenAPI**: http://localhost:8080/swagger-ui.html
- **MySQL Docs**: https://dev.mysql.com/doc/

---

**Last Updated**: April 1, 2026
**Version**: 1.0.0

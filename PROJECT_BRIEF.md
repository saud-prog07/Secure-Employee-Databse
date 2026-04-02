# Secure Employee Management System (EMS) - Complete Project Brief

## Project Overview
An enterprise-grade, full-stack application for secure organizational management with role-based access control, two-factor authentication, payroll management, and comprehensive workflow automation.
Backend: Java Spring Boot 3.2.4 REST API with JWT & Spring Security
Frontend: React.js with modern responsive UI
Database: MySQL 8.0 with audit logging
Authentication: JWT + Two-Factor Authentication (TOTP/Google Authenticator)
Architecture: Stateless, microservices-ready design

## Technology Stack
Backend
Framework: Spring Boot 3.2.4 | Language: Java 17
Security: Spring Security 6 + JWT (JJWT) + 2FA (Google Authenticator)
Database: MySQL 8.0 | ORM: Hibernate JPA
Notifications: Spring Mail (SMTP) + Twilio SMS
API Docs: OpenAPI 3.0 / Swagger
Build: Maven | Logging: SLF4J
Frontend
Framework: React.js (Create React App) | Routing: React Router v6
HTTP: Axios with interceptors | Styling: CSS3 + Design System
Package Manager: npm
Infrastructure
Containerization: Docker | Orchestration: Docker Compose
Ports: Backend (8080), Frontend (3000), MySQL (3307)

## Complete Features Overview

### 1. User Authentication & Authorization (Two-Factor Auth)

#### JWT-based Authentication
Secure login with JWT tokens (HS512 algorithm)
Auto token refresh handling
Token stored securely in localStorage
Auto-logout on token expiration (401)

#### Two-Factor Authentication (2FA)
TOTP-based (Time-based One-Time Password)
Compatible with Google Authenticator, Microsoft Authenticator, Authy
Automatic QR code generation for easy setup
6-digit codes with 30-second validity window
Disable 2FA option for users

#### Role-Based Access Control (RBAC)
ADMIN: Full system access, approve/reject users and employees
HR: Employee management, view payroll
PENDING: Limited access until admin approval
Protected routes with automatic redirection

### 2. User Management System

#### User Registration
Self-registration with email validation
Username uniqueness checking
Password strength enforcement (BCrypt hashing)
Status: PENDING → APPROVED (admin approval required)
Automatic registration email notification

#### Admin User Management
View all registered users with status
Approve/disapprove user registrations
Create users directly (no approval needed)
Soft-delete users (can restore)
Permanent hard-delete option
Safety checks (prevent self-deletion)

### 3. Employee Management

#### Full CRUD Operations
Create employees (HR submits, ADMIN approves)
View employee directory with pagination
Search and filter by multiple criteria
Edit employee details (name, email, department, salary)
Soft-delete employees (archive, can restore)
Status tracking: PENDING → APPROVED → REJECTED

#### Employee Approval Workflow
Employees start as PENDING after creation
ADMIN review and approve/reject
Track who approved and when
Automatic email notification on approval/rejection
Rejection reason tracking

### 4. Payroll Management System

#### Payroll Generation & Calculation
Create payroll records for employees
Formula: Final Salary = Base Salary + Bonus - Deductions
Month-based payroll (YYYY-MM format)
Prevent duplicate payroll for same employee/month
Edit and delete payroll records (ADMIN only)

#### Payroll Views & Reports
View all payroll records (ADMIN & HR)
Filter by employee and month
View payroll history per employee
Currency formatting with color coding
Calculation transparency (show all components)

#### Access Control
ADMIN: Full payroll management (create, edit, delete)
HR: Read-only access to payroll data
Others: No access

### 5. Workflow Automation for Approvals

#### Multi-Stage Approval System
Employee creation triggers PENDING status
ADMIN dashboard shows pending approvals with count
One-click approve/reject buttons
Track approver name and timestamp
Automatic email notifications on action

#### Status Tracking
PENDING (yellow): Awaiting review
APPROVED (green): Successfully approved
REJECTED (red): Rejected, requires revision

### 6. Third-Party Integration (Email & SMS)

#### Email Notifications (SMTP)
Registration: Welcome email sent automatically
Approval: Email notifies employee of approval
Rejection: Email explains rejection, directs to HR
Login: Optional 2FA OTP email
Support: Test endpoints available

#### SMS Notifications (Twilio)
OTP codes via SMS for 2FA
Approval/rejection alerts (optional)
Login notifications
Flexible configuration (optional feature)

#### Configuration
Gmail SMTP support (free, no cost)
SendGrid integration option
Twilio for SMS (pay-per-use)
All credentials in environment variables (secure)

### 7. Audit & Logging

#### Comprehensive Audit Trail
Track ALL actions: Create, Update, Delete, Approve, Reject, Login, Register
Timestamp for every action
Username of performer
Action type and details
ADMIN-only audit log viewer with pagination
Export audit logs (future feature)

#### Security Logging
2FA setup and verification attempts
Login success/failure tracking
Approval workflow actions
Email/SMS sending status
API error logging

### 8. User Profile Management

#### Profile Features
View personal profile information
Display username, role, status
Change password functionality
Profile picture placeholder (future)
Account security status

### 9. Dashboard & Analytics

#### ADMIN Dashboard
Pending approvals widget (count + preview)
Key metrics (total users, employees, pending)
Quick action buttons
Status distribution chart
Recent activity feed

#### HR Dashboard
Assigned employees
Team statistics
Pending tasks
Payroll shortcuts

### 10. QR-Based Attendance System (Public Access)

#### Key Features
Public access - No login required to scan attendance
QR code simulation via employee ID input
One login per calendar day per employee
Real-time attendance status checking
Automatic duplicate scan prevention

#### Scan Workflow
Employee enters their ID (or scans actual QR code)
System validates employee exists
Checks if already logged in today
If first login: Records login time, marks as PRESENT
If already logged in: Returns "Already logged in at [time]"

#### Employee Views
Attendance page: `/attendance` (publicly accessible)
Input field for employee ID
"Scan Attendance" button
"Check Status" button for status verification
Real-time feedback with employee name and exact time
Previous login display if already present

#### Admin Features
View today's attendance: `GET /api/attendance/today`
Get employee history: `GET /api/attendance/history/{employeeId}`
Date range queries: `GET /api/attendance/range/{employeeId}`
Audit logging of all scans

### 11. QR Code Generation System

#### QR Code Features
Generate unique QR codes for each employee
Encode employee ID in QR code format (EMP-{id})
Two output formats: PNG image or base64 data URL
Downloadable QR code for employee identification
Secure access with user authentication

#### API Endpoints
GET `/api/v1/employees/{id}/qr` - Generate QR code (PNG image)
GET `/api/v1/employees/{id}/qr/data-url` - Get QR as base64 URL

#### Frontend Features
"QR Code" button in employee list actions
Modal display of QR code with employee name
"Download QR Code" button for saving to file
Useful for: Attendance systems, access control, ID badges, asset tracking

#### Use Cases
Attendance scanning (integrated with attendance system)
Employee ID badges
Access control systems
Document authentication
Inventory management
Visitor tracking

### 12. Attendance Analytics System

#### Analytics Features
Calculate overall attendance percentage for each employee
Track total days recorded in system
Count present vs absent days
Automatic status assignment: GOOD (75%+), AVERAGE (60-74%), POOR (<60%)
Monthly attendance summaries
Used for promotion and performance evaluation logic

#### API Endpoints
GET `/api/attendance/summary/{employeeId}` - Get overall attendance summary
GET `/api/attendance/summary/{employeeId}/month/{YYYY-MM}` - Get monthly summary

#### Analytics Response Format
employeeId: Employee ID
employeeName: Employee full name
totalDays: Total days in attendance records
presentDays: Count of days present
absentDays: Count of days absent
attendancePercentage: Calculated percentage (0-100)
status: GOOD / AVERAGE / POOR based on percentage

#### Frontend Display
Integrated in employee profile/details page
Large percentage display with color coding
Present vs Absent day counters
Progress bar visualization
Status badge with performance indicator
Useful for: Performance reviews, promotion decisions, employee evaluations

#### Use Cases
Performance evaluation and promotion decisions
Identifying attendance issues
Employee scorecard generation
HR reporting and analytics
Compliance and regulatory requirements

### 13. Employee Workday Statistics System

#### Workday Features
Calculate total workdays per year (excluding weekends and holidays)
Track employee work attendance against calculated workdays
Integrate with Windows system calendar for automatic holiday detection
Display worked days vs total workdays in year
Performance status classification: EXCELLENT (90%+), GOOD (75-89%), AVERAGE (60-74%), POOR (<60%)
Multi-year support (2024, 2025, 2026, 2027, etc.)

#### Smart Calculations
Total Workdays = 365 days - weekends (Sat/Sun) - holidays
Automatically excludes:
- All Saturdays and Sundays
- Public holidays from holiday registry
- Custom company holidays (configurable)

#### API Endpoints
GET `/api/workday/stats/{employeeId}?year=2026` - Get workday statistics for employee
GET `/api/workday/current-year` - Get current year for auto-population

#### Frontend Features
Employee Workday page accessible from navbar (ADMIN & HR only)
View all employees with workday statistics
Year selector (2024-2027) with dynamic re-calculation
Filter by performance status: EXCELLENT, GOOD, AVERAGE, POOR
Employee cards showing:
- Employee name, email, department
- Total workdays in year
- Present days (verified attendance)
- Absent days (calculated: total workdays - present days)
- Attendance percentage with progress bar
- Performance status badge (color-coded)
- Weekend days count for the year
- Public holidays count for the year

#### Database Schema
holidays table stores:
- date: Holiday date
- name: Holiday name (e.g., "Christmas")
- description: Holiday description
- year: Calendar year
- Unique constraint on date (prevents duplicates)
- Indexed by year for fast queries

Pre-populated with US holidays: New Year, MLK Day, Presidents Day, Memorial Day, Independence Day, Labor Day, Thanksgiving, Christmas, etc.

#### Access Control
ADMIN: Full access to view all employee workday statistics
HR: Full access to view all employee workday statistics
OTHERS: No access (403 Forbidden)

#### Use Cases
Performance evaluation and promotion decisions
HR analytics and reporting
Identifying employment patterns
Compliance verification
Contract renewal decisions
Bonus/incentive calculations based on workday attendance


#### Backend Security
BCrypt password hashing (strength 12)
JWT signing with HS512 algorithm
CORS enabled for frontend domain only
SQL injection protection (JPA/Hibernate)
CSRF token handling
HTTP-only cookie option (future)

#### Frontend Security
Token stored in secure localStorage
XSS protection (React auto-escaping)
Input validation on all forms
HTTPS recommended for production
No sensitive data in URLs

Data Protection
Soft-delete for data recovery
Restoration option for deleted records
Audit trail for compliance
PII handling according to best practices

## Quick Start (5 minutes)

### Prerequisites
Java 17+
Node.js 16+
MySQL 8.0
Docker (optional)

### Option 1: Docker (Recommended)
```bash
# Start database
docker compose up -d

# Backend
mvn clean install -DskipTests
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start
```

### Option 2: Manual Setup
```bash
# Start MySQL, then:
mvn clean install -DskipTests
mvn spring-boot:run

# New terminal:
cd frontend
npm install
npm start
```

### Default Credentials
ADMIN: username: `admin` | password: `admin`
HR: username: `hr` | password: `hr`

## Core API Endpoints

### Authentication
Method	Endpoint	Purpose
POST	`/api/auth/register`	User registration
POST	`/api/auth/login`	Login (returns JWT or OTP_REQUIRED)
POST	`/api/auth/verify-otp`	Verify 2FA code
POST	`/api/auth/2fa/setup`	Generate QR code for 2FA
POST	`/api/auth/2fa/confirm`	Enable 2FA
POST	`/api/auth/2fa/disable`	Disable 2FA

### Users & Approvals
Method	Endpoint	Role	Purpose
GET	`/api/v1/users`	ADMIN	List all users
PUT	`/api/v1/users/{id}/approve`	ADMIN	Approve user
PUT	`/api/v1/users/{id}/disapprove`	ADMIN	Disapprove user
DELETE	`/api/v1/users/{id}`	ADMIN	Delete user

### Employees & Workflow
Method	Endpoint	Role	Purpose
GET	`/api/v1/employees`	Any	List employees
POST	`/api/v1/employees`	HR	Create employee
PUT	`/api/v1/employees/{id}`	HR	Edit employee
PUT	`/api/v1/employees/{id}/approve`	ADMIN	Approve employee
PUT	`/api/v1/employees/{id}/reject`	ADMIN	Reject employee
DELETE	`/api/v1/employees/{id}`	ADMIN	Delete employee
GET	`/api/v1/employees/approvals/pending`	ADMIN	Get pending

### Payroll
Method	Endpoint	Role	Purpose
POST	`/api/payroll/generate`	ADMIN	Create payroll
GET	`/api/payroll`	ADMIN, HR	List payroll
GET	`/api/payroll/{id}`	ADMIN, HR	Get payroll
GET	`/api/payroll/employee/{id}`	ADMIN, HR	Employee history
PUT	`/api/payroll/{id}`	ADMIN	Update payroll
DELETE	`/api/payroll/{id}`	ADMIN	Delete payroll

### Attendance (Public & Secured Endpoints)
Method	Endpoint	Auth	Purpose
POST	`/api/attendance/scan`	PUBLIC	Scan attendance
GET	`/api/attendance/status/{employeeId}`	PUBLIC	Check login status
GET	`/api/attendance/history/{employeeId}`	ADMIN, HR	View history
GET	`/api/attendance/today`	ADMIN, HR	Today's attendance
GET	`/api/attendance/range/{employeeId}`	ADMIN, HR	Date range query

### QR Code Generation
Method	Endpoint	Auth	Purpose
GET	`/api/v1/employees/{id}/qr`	USER, HR, ADMIN	Generate QR (PNG)
GET	`/api/v1/employees/{id}/qr/data-url`	USER, HR, ADMIN	Get QR (base64 URL)

### Notifications
Method	Endpoint	Role	Purpose
POST	`/api/notifications/email/send`	ADMIN	Send email
POST	`/api/notifications/sms/send`	ADMIN	Send SMS
POST	`/api/notifications/test/email/{email}`	ADMIN	Test email config

### Audit
Method	Endpoint	Role	Purpose
GET	`/api/admin/audit`	ADMIN	View audit logs

## Key Implementation Details

### Database Schema (Key Tables)
users: System users with approval workflow
employees: Employee records with approval status
payroll: Payroll records (base salary, bonus, deductions, final salary)
attendance: Daily attendance records with login timestamps
audit_logs: Complete action history
roles: User roles (ADMIN, HR)

### Frontend Routes (Protected by Role)
`/` - Public landing
`/login` - Public login
`/register` - Public registration
`/verify-otp` - 2FA verification
`/attendance` - PUBLIC: QR attendance system (no login required)
`/dashboard` - ADMIN/HR dashboard
`/employees` - Employee management
`/employees/add` - Create employee
`/employees/edit/:id` - Edit employee
`/payroll/list` - View payroll
`/payroll/generate` - Create payroll (ADMIN only)
`/profile` - User profile
`/audit-logs` - Audit viewer (ADMIN only)

## Project Structure
```
Secure-Employee-Management-System-API/
├── src/
│   ├── main/
│   │   ├── java/com/example/employee/
│   │   │   ├── config/          # Security, OpenAPI config
│   │   │   ├── controller/      # REST endpoints
│   │   │   ├── service/         # Business logic (email, SMS, payroll, 2FA)
│   │   │   ├── entity/          # Models (User, Employee, Payroll)
│   │   │   ├── repository/      # Data access
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── security/        # JWT, 2FA logic
│   │   │   └── specification/   # JPA criteria queries
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # Unit & integration tests
├── frontend/
│   ├── src/
│   │   ├── pages/              # React components (Login, Dashboard, etc.)
│   │   ├── services/           # API integration, auth handling
│   │   ├── App.js              # Router, main app
│   │   └── index.css           # Global styles
│   └── package.json
├── docker-compose.yml          # MySQL container config
├── pom.xml                     # Maven dependencies
└── DATABASE_MIGRATION_SCRIPTS/
    ├── db-migration-2fa.sql
    ├── db-migration-approval-workflow.sql
    ├── db-migration-payroll.sql
    ├── db-migration-attendance.sql
    └── ...
```
---

Important Notes & Best Practices

Security
All API keys/credentials stored in environment variables (never hardcoded)
Passwords hashed with BCrypt (strength 12)
JWT tokens expire after 24 hours
CORS restricted to frontend origin only
2FA adds extra security layer

Database
Run migrations before first deployment
Backup before schema changes
Soft-delete preserves data for compliance

Testing
Unit tests for services
Integration tests for endpoints
Manual testing scenarios in dedicated guides

Deployment
Use environment variables for configs
Docker Compose for local dev
Kubernetes-ready (future enhancement)
---

Next Steps
Quick Start: Follow docker-compose setup above
Database Migration: Run all SQL scripts in order
Test All Features: Use test credentials (admin/admin, hr/hr)
Configure Email/SMS: Add your API keys to environment
Deploy: Build and run with your infrastructure
---

Complete Feature Breakdown
See SETUP_AND_DEVELOPER_GUIDE.md for:
Detailed API endpoint documentation
Setup and deployment instructions
Testing procedures (unit, integration, manual)
Troubleshooting and FAQ
Configuration examples
Performance optimization tips
JWT token-based authentication
Authorization headers in all requests
Automatic token inclusion via Axios interceptor
401 error handling with auto-redirect to login
CORS ready configuration
9. Frontend UI/UX
Responsive design
Professional gradient backgrounds
Card-based layout system
Color-coded badges for status
Error and success message displays
Loading states
Navigation navbar (sticky)
Logout functionality
Role-based navigation (admin features hidden from HR)
10. Database Integration
Entity relationships properly defined
Soft delete implementation (deleted flag)
Timestamps (created_at, updated_at)
Unique constraints (username, email)
Enum types for Role and Status
JPA repositories for database operations
Database migration support (DDL auto)
---

API Endpoints
Authentication
```
POST   /api/auth/login          - Login user, return JWT token
POST   /api/auth/register       - Register new user (requires admin approval)
```
User Management (Admin Only)
```
GET    /api/admin/users         - List all users
POST   /api/admin/users/create  - Create user directly
PUT    /api/admin/users/{id}/approve    - Approve pending user
PUT    /api/admin/users/{id}/disapprove - Revoke approval
DELETE /api/admin/users/{id}    - Soft delete user
DELETE /api/admin/users/{id}/permanent - Hard delete user
```
Employee Management
```
GET    /api/employees           - List all employees (paginated)
GET    /api/employees/{id}      - Get employee details
POST   /api/employees           - Create new employee
PUT    /api/employees/{id}      - Update employee
DELETE /api/employees/{id}      - Soft delete employee
GET    /api/employees/search    - Search employees
```
User Profile & Security
```
GET    /api/profile             - Get current user profile
PUT    /api/profile/password    - Change password
```
Audit Logging (Admin Only)
```
GET    /api/admin/audit-logs    - View all audit logs
GET    /api/admin/audit-logs/user/{username} - User-specific logs
```
---
Project Structure
```
├── frontend/                          # React Frontend
│   ├── src/
│   │   ├── pages/
│   │   │   ├── Login.js              # Login page
│   │   │   ├── Register.js           # Registration page
│   │   │   ├── Dashboard.js          # Main dashboard
│   │   │   ├── AddEmployee.js        # Create employee form
│   │   │   ├── EditEmployee.js       # Edit employee form
│   │   │   ├── ManageUsers.js        # Admin user management
│   │   │   ├── AuditLogs.js          # Audit log viewer
│   │   │   └── Profile.js            # User profile & password change
│   │   ├── services/
│   │   │   └── api.js                # Axios configuration & interceptors
│   │   └── App.js                    # Router setup
│   └── package.json
│
├── src/main/java/                     # Java Backend
│   └── com/example/employee/
│       ├── EmployeeApplication.java   # Spring Boot entry point
│       ├── controller/                # REST API endpoints
│       │   ├── AuthController.java
│       │   ├── EmployeeController.java
│       │   ├── UserController.java
│       │   ├── AdminUserController.java
│       │   └── AuditController.java
│       ├── service/                   # Business logic
│       │   ├── EmployeeService.java
│       │   ├── UserService.java
│       │   ├── AuditLogService.java
│       │   └── CustomUserDetailsService.java
│       ├── entity/                    # Database entities
│       │   ├── User.java
│       │   ├── Employee.java
│       │   ├── AuditLog.java
│       │   ├── Role.java
│       │   └── EmployeeStatus.java
│       ├── repository/                # Database access
│       ├── security/                  # JWT & Auth
│       │   ├── JwtUtils.java
│       │   └── JwtAuthenticationFilter.java
│       ├── config/                    # Application config
│       │   ├── SecurityConfig.java
│       │   ├── OpenApiConfig.java
│       │   └── DataLoader.java
│       └── dto/                       # Data transfer objects
│           ├── LoginRequest.java
│           ├── RegisterRequest.java
│           ├── JwtResponse.java
│           └── ApiResponse.java
│
├── pom.xml                            # Maven dependencies
├── Dockerfile                         # Docker image config
├── docker-compose.yml                 # MySQL container config
└── README.md                          # Documentation
```
---
How to Run
Prerequisites
Java 17
Maven 3.9+
Node.js & npm
Docker & Docker Compose
Start the Application
Build Backend
```bash
   mvn clean package -DskipTests
   ```
Start Database
```bash
   docker-compose up -d
   ```
Start Backend API (runs on port 8080)
```bash
   java -jar target/employee-management-system-0.0.1-SNAPSHOT.jar
   ```
Start Frontend (runs on port 3000 or 3001)
```bash
   npm start --prefix frontend
   ```
Access the Application
Frontend: http://localhost:3000
API Docs: http://localhost:8080/swagger-ui.html
Database: localhost:3307 (MySQL)
---
Default Credentials
An admin user is automatically created on first run:
Username: admin
Password: admin (configured in DataLoader)
Role: ADMIN
---
Security Features
Password Security: BCrypt hashing with salt
Token Security: JWT with expiration (24 hours)
Authorization: Role-based access control
Approval Workflow: Two-step user activation
Audit Trail: Complete action logging
Soft Delete: Data recovery capability
API Security: Token validation on every request
Input Validation: Server-side validation on all inputs
---

## Database Schema

### Users Table
id, username, password, role, approved, deleted

### Employees Table
id, name, email, department, salary, status, created_at, updated_at, deleted

### Audit Logs Table
id, username, action, timestamp

### Relationships
One User can perform multiple audit logs
One User can manage multiple Employees
Employee deletion is soft (only marked deleted)

## Workflow Examples

### User Registration & Approval Flow
User clicks "Create Account"
Fills registration form with username & password
Account created with `approved = false`
Admin sees pending approval on user management page
Admin clicks "Approve"
User can now login

### Change Password Flow
User logs in successfully
Goes to "My Profile" page
Enters new password (min 5 chars) and confirmation
Clicks "Update Password"
Password updated and stored as BCrypt hash

### Employee CRUD Flow
HR/Admin logs in
Dashboard shows employee count
Click "Add Employee" to create new record
View all employees with pagination
Click "Edit" to modify details
Can soft-delete or permanently remove

## Statistics & Metrics
Total API Endpoints: 15+
Database Tables: 4
Frontend Components: 7 pages
Security Layers: 3 (Password, JWT, Role-based)
Audit Events: 6+ tracked actions

## What Makes This System Production-Ready
Enterprise Security - BCrypt hashing, JWT tokens, role-based access
Audit Compliance - Complete action logging for HR regulations
User Management - Approval workflow prevents unauthorized access
Data Integrity - Soft deletes, timestamps, proper relationships
Scalability - Stateless architecture, database-driven
Error Handling - Comprehensive exception handling
API Documentation - OpenAPI/Swagger integration
Docker Support - Easy deployment and environment consistency

## Version Info
Created: March 31, 2026
Backend Version: 0.0.1-SNAPSHOT
Spring Boot: 3.2.4
Java: 17
React: Latest (Create React App)
MySQL: 8.0

## Repository
https://github.com/saud-prog07/Secure-Employee-Databse.git

## Status
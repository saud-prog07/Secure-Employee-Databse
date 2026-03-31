# Secure Employee Management System - Project Brief

## 📋 Project Overview

A **full-stack web application** for managing employees and system users with role-based access control, secure authentication, and comprehensive audit logging.

- **Backend**: Java Spring Boot 3.2.4 REST API
- **Frontend**: React.js with modern UI
- **Database**: MySQL 8.0
- **Architecture**: Microservices-ready, stateless JWT authentication

---

## 🏗️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.4
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Security**: Spring Security + JWT Token
- **ORM**: Hibernate JPA
- **Build Tool**: Maven
- **API Docs**: OpenAPI/Swagger

### Frontend
- **Framework**: React.js (Create React App)
- **Routing**: React Router v6
- **HTTP Client**: Axios
- **Styling**: CSS3 + Custom Variables
- **Package Manager**: npm

### Infrastructure
- **Containerization**: Docker
- **Database Container**: MySQL Docker Image
- **Port Configuration**: Backend (8080), Frontend (3000/3001), DB (3307)

---

## 🔑 Key Features Implemented

### 1. **User Authentication & Authorization**
- ✅ Secure login with JWT tokens
- ✅ Role-based access control (ADMIN, HR)
- ✅ Token stored in localStorage with automatic cleanup on logout
- ✅ JWT token refresh handling
- ✅ Protected routes for authenticated users
- ✅ Admin-only route restrictions

### 2. **User Registration System**
- ✅ Self-registration form with validation
- ✅ Username uniqueness checking
- ✅ Password encryption (BCrypt)
- ✅ Approval workflow - new registrations marked as pending
- ✅ Admin approval required before user can login
- ✅ Clear feedback messages about approval status

### 3. **User Management (Admin Only)**
- ✅ View all registered users
- ✅ See approval status for each user
- ✅ Approve pending user registrations
- ✅ Disapprove approved users
- ✅ Deactivate users (soft delete)
- ✅ Permanent delete users
- ✅ Direct user creation by admin
- ✅ Bulk user status management
- ✅ Pending approvals highlighted at top of dashboard

### 4. **Employee Management**
- ✅ Full CRUD operations (Create, Read, Update, Delete)
- ✅ Employee fields: Name, Email, Department, Salary, Status
- ✅ Employee status tracking (PENDING, APPROVED)
- ✅ Pagination support for large datasets
- ✅ Filter and search capabilities
- ✅ Edit employee information
- ✅ Soft delete with restoration option

### 5. **Account Security**
- ✅ Password change functionality for logged-in users
- ✅ Password validation rules (min 5 characters)
- ✅ Confirmation password matching
- ✅ Secure password transmission via backend
- ✅ BCrypt password hashing
- ✅ Auto-logout on token expiration (401 errors)

### 6. **Audit & Logging**
- ✅ Complete audit trail of all actions
- ✅ Tracked events: Register, Login, Create, Update, Delete, Approve
- ✅ Timestamp for each action
- ✅ Username of person who performed action
- ✅ Audit log viewer (Admin only)
- ✅ Database persistence of all audit logs

### 7. **User Profile Management**
- ✅ View personal profile information
- ✅ Display username and role
- ✅ Show account status (Verified/Pending)
- ✅ Change password from profile
- ✅ Secure profile endpoint

### 8. **API Security**
- ✅ JWT token-based authentication
- ✅ Authorization headers in all requests
- ✅ Automatic token inclusion via Axios interceptor
- ✅ 401 error handling with auto-redirect to login
- ✅ CORS ready configuration

### 9. **Frontend UI/UX**
- ✅ Responsive design
- ✅ Professional gradient backgrounds
- ✅ Card-based layout system
- ✅ Color-coded badges for status
- ✅ Error and success message displays
- ✅ Loading states
- ✅ Navigation navbar (sticky)
- ✅ Logout functionality
- ✅ Role-based navigation (admin features hidden from HR)

### 10. **Database Integration**
- ✅ Entity relationships properly defined
- ✅ Soft delete implementation (deleted flag)
- ✅ Timestamps (created_at, updated_at)
- ✅ Unique constraints (username, email)
- ✅ Enum types for Role and Status
- ✅ JPA repositories for database operations
- ✅ Database migration support (DDL auto)

---

## 📊 API Endpoints

### Authentication
```
POST   /api/auth/login          - Login user, return JWT token
POST   /api/auth/register       - Register new user (requires admin approval)
```

### User Management (Admin Only)
```
GET    /api/admin/users         - List all users
POST   /api/admin/users/create  - Create user directly
PUT    /api/admin/users/{id}/approve    - Approve pending user
PUT    /api/admin/users/{id}/disapprove - Revoke approval
DELETE /api/admin/users/{id}    - Soft delete user
DELETE /api/admin/users/{id}/permanent - Hard delete user
```

### Employee Management
```
GET    /api/employees           - List all employees (paginated)
GET    /api/employees/{id}      - Get employee details
POST   /api/employees           - Create new employee
PUT    /api/employees/{id}      - Update employee
DELETE /api/employees/{id}      - Soft delete employee
GET    /api/employees/search    - Search employees
```

### User Profile & Security
```
GET    /api/profile             - Get current user profile
PUT    /api/profile/password    - Change password
```

### Audit Logging (Admin Only)
```
GET    /api/admin/audit-logs    - View all audit logs
GET    /api/admin/audit-logs/user/{username} - User-specific logs
```

---

## 🗂️ Project Structure

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

## 🚀 How to Run

### Prerequisites
- Java 17
- Maven 3.9+
- Node.js & npm
- Docker & Docker Compose

### Start the Application

1. **Build Backend**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Start Database**
   ```bash
   docker-compose up -d
   ```

3. **Start Backend API** (runs on port 8080)
   ```bash
   java -jar target/employee-management-system-0.0.1-SNAPSHOT.jar
   ```

4. **Start Frontend** (runs on port 3000 or 3001)
   ```bash
   npm start --prefix frontend
   ```

### Access the Application
- **Frontend**: http://localhost:3000
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Database**: localhost:3307 (MySQL)

---

## 📝 Default Credentials

An admin user is automatically created on first run:
- **Username**: admin
- **Password**: admin (configured in DataLoader)
- **Role**: ADMIN

---

## 🔒 Security Features

- ✅ **Password Security**: BCrypt hashing with salt
- ✅ **Token Security**: JWT with expiration (24 hours)
- ✅ **Authorization**: Role-based access control
- ✅ **Approval Workflow**: Two-step user activation
- ✅ **Audit Trail**: Complete action logging
- ✅ **Soft Delete**: Data recovery capability
- ✅ **API Security**: Token validation on every request
- ✅ **Input Validation**: Server-side validation on all inputs

---

## 📈 Database Schema

### Users Table
- id, username, password, role, approved, deleted

### Employees Table
- id, name, email, department, salary, status, created_at, updated_at, deleted

### Audit Logs Table
- id, username, action, timestamp

### Relationships
- One User can perform multiple audit logs
- One User can manage multiple Employees
- Employee deletion is soft (only marked deleted)

---

## 🔄 Workflow Examples

### User Registration & Approval Flow
1. User clicks "Create Account"
2. Fills registration form with username & password
3. Account created with `approved = false`
4. Admin sees pending approval on user management page
5. Admin clicks "Approve"
6. User can now login

### Change Password Flow
1. User logs in successfully
2. Goes to "My Profile" page
3. Enters new password (min 5 chars) and confirmation
4. Clicks "Update Password"
5. Password updated and stored as BCrypt hash

### Employee CRUD Flow
1. HR/Admin logs in
2. Dashboard shows employee count
3. Click "Add Employee" to create new record
4. View all employees with pagination
5. Click "Edit" to modify details
6. Can soft-delete or permanently remove

---

## 📊 Statistics & Metrics

- **Total API Endpoints**: 15+
- **Database Tables**: 4
- **Frontend Components**: 7 pages
- **Security Layers**: 3 (Password, JWT, Role-based)
- **Audit Events**: 6+ tracked actions

---

## 🎯 What Makes This System Production-Ready

1. **Enterprise Security** - BCrypt hashing, JWT tokens, role-based access
2. **Audit Compliance** - Complete action logging for HR regulations
3. **User Management** - Approval workflow prevents unauthorized access
4. **Data Integrity** - Soft deletes, timestamps, proper relationships
5. **Scalability** - Stateless architecture, database-driven
6. **Error Handling** - Comprehensive exception handling
7. **API Documentation** - OpenAPI/Swagger integration
8. **Docker Support** - Easy deployment and environment consistency

---

## 📌 Version Info
- **Created**: March 31, 2026
- **Backend Version**: 0.0.1-SNAPSHOT
- **Spring Boot**: 3.2.4
- **Java**: 17
- **React**: Latest (Create React App)
- **MySQL**: 8.0

---

## 🔗 Repository
https://github.com/saud-prog07/Secure-Employee-Databse.git

---

**Status**: ✅ Fully Functional & Ready for Enhancement

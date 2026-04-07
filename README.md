# Secure Employee Management System (EMS)

An enterprise-grade Spring Boot and React application for secure organizational management with comprehensive role-based access control (RBAC), soft-delete restoration, and detailed administrative auditing.

## Table of Contents

- [Core Features](#core-features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Running Tests](#running-tests)
- [Security](#security)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Core Features

- **Modern Design System**: Fully responsive UI with a Slate/Indigo theme, micro-animations, and professional layouts.
- **Bi-Directional Approvals**: Admins can approve, reject, or disapprove employee records and HR/Administrator accounts.
- **Dynamic Record Editing**: HR and Admins can modify existing employee professional credentials in real-time.
- **Soft-Delete & Archive Recovery**: Deleted records are logically archived and can be restored by administrators at any time.
- **Hard Delete Capability**: Permanent removal of user accounts for system maintenance when needed.
- **Account Protection**: Built-in safety logic prevents administrators from accidentally deactivating or deleting their own accounts.
- **Comprehensive Audit Trail**: Every sensitive action (approvals, rejections, deletions, restores, logins) is tracked with timestamps and user attribution.
- **Two-Factor Authentication (2FA)**: Secure login process with OTP verification via email.
- **Role-Based Access Control (RBAC)**: Granular permission management for Admin, HR, and Employee roles.

## Tech Stack

### Backend
- **Core**: Java 17, Spring Boot 3.2
- **Security**: Spring Security 6, JWT (JJWT)
- **Database**: MySQL 8.0, Hibernate JPA
- **API Documentation**: Swagger/OpenAPI 3.0
- **Build Tool**: Maven 3.8+

### Frontend
- **Framework**: React.js, React Router 6
- **Styling**: Vanilla CSS with CSS Variables
- **HTTP Client**: Axios with interceptors for request/response handling
- **Package Manager**: npm

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17 or higher** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Node.js 16.x or higher** and npm 8.x - [Download](https://nodejs.org/)
- **Docker & Docker Compose** (recommended for database) - [Download](https://www.docker.com/)
- **MySQL 8.0** (alternative to Docker) - [Download](https://www.mysql.com/)
- **Maven 3.8+** (for building backend)
- **Git** for version control

### Port Requirements

- **Backend API**: `8080` (default)
- **Frontend**: `3000` (default)
- **MySQL Database**: `3307` (Docker) or `3306` (local)
- **Swagger UI**: `8080/swagger-ui.html`

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Secure-Employee-Management-System-API-
```

### 2. Database Setup (Docker - Recommended)

```bash
# Start MySQL database using Docker Compose
docker compose up -d
```

This will start MySQL 8.0 on port **3307** and auto-seed the database with initial data.

**Alternatively, use local MySQL:**

```bash
# Create database
mysql -u root -p < db-migration-master.sql
```

### 3. Backend Setup

```bash
# Install dependencies and build
mvn clean install -DskipTests

# Run the application (auto-seeds default accounts on first launch)
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

**Default Credentials (change immediately in production):**
- Admin: `admin` / `admin`
- HR: `hr` / `hr`

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

The frontend will open at `http://localhost:3000`

## Configuration

### Backend Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server Port
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3307/employee_db
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT Configuration
jwt.secret=<your-secret-key>
jwt.expiration=3600000

# SMTP Configuration (for 2FA OTP emails)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<your-email>
spring.mail.password=<your-app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Application Name
app.name=Employee Management System
```

### Frontend Configuration

Update the API base URL in `frontend/src/services/api.js` to match your backend:

```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

## API Documentation

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Core API Endpoints

| Endpoint | Method | Access Level | Description |
|----------|--------|--------------|-------------|
| `/api/auth/register` | POST | Public | New HR registration request |
| `/api/auth/login` | POST | Public | User login with 2FA |
| `/api/auth/verify-otp` | POST | Public | Verify OTP for 2FA |
| `/api/v1/employees` | GET | Authenticated | List authorized employees |
| `/api/v1/employees` | POST | HR/ADMIN | Create new employee |
| `/api/v1/employees/{id}` | PUT | HR/ADMIN | Edit employee professional details |
| `/api/v1/employees/{id}` | DELETE | ADMIN | Soft delete employee |
| `/api/v1/employees/{id}/restore` | PUT | ADMIN | Restore soft-deleted employee |
| `/api/v1/employees/{id}/reject` | PUT | ADMIN | Revert employee approval status |
| `/api/admin/users` | GET | ADMIN | List system users |
| `/api/admin/users/{id}/disapprove` | PUT | ADMIN | Revoke system access |
| `/api/admin/audit` | GET | ADMIN | View system audit logs |
| `/api/attendance` | POST | Any | Record attendance |
| `/api/payroll` | GET | HR/ADMIN | View payroll records |

## Project Structure

```
Secure-Employee-Management-System-API-/
├── src/main/java/com/example/employee/
│   ├── config/              # Spring configuration, security, OpenAPI
│   ├── controller/          # REST API endpoints
│   ├── dto/                 # Data Transfer Objects
│   ├── entity/              # JPA entities
│   ├── exception/           # Custom exception handling
│   ├── repository/          # Database access layer
│   ├── security/            # Security filters, JWT handling
│   ├── service/             # Business logic
│   └── specification/       # JPA query specifications
├── src/main/resources/
│   └── application.properties  # Backend configuration
├── frontend/
│   ├── src/
│   │   ├── components/      # Reusable React components
│   │   ├── pages/           # Page components (Login, Dashboard, etc.)
│   │   ├── services/        # API client service
│   │   └── styles/          # CSS stylesheets
│   └── package.json         # Frontend dependencies
├── pom.xml                  # Maven configuration
├── docker-compose.yml       # Docker setup for database
└── README.md               # This file
```

## Running Tests

### Backend Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EmployeeServiceTest

# Run tests with coverage
mvn test jacoco:report
```

Test results are generated in `target/site/jacoco/index.html`

### Frontend Tests

```bash
cd frontend

# Run all tests
npm test

# Run with coverage
npm test -- --coverage
```

## Security

### Authentication & Authorization

- **JWT-based authentication** with Spring Security
- **Role-Based Access Control (RBAC)** with three roles: Admin, HR, Employee
- **Two-Factor Authentication (2FA)** via email OTP for enhanced security
- **Password hashing** using BCrypt
- **CORS configuration** restricted to allowed domains
- **Audit logging** of all administrative actions

### Important Security Notes

1. **Change Default Credentials**: Update the default admin and HR credentials immediately in production.
2. **JWT Secret**: Update `jwt.secret` in `application.properties` with a strong, unique key.
3. **Email Configuration**: Configure SMTP settings for 2FA OTP emails.
4. **HTTPS**: Enable HTTPS in production environments.
5. **Environment Variables**: Store sensitive data (passwords, API keys) in environment variables, not hardcoded.

### Two-Factor Authentication Setup

2FA is enabled by default. Users must verify an OTP sent to their registered email during login:

1. User enters username and password
2. System sends OTP to registered email
3. User enters OTP within the time window
4. Session is created upon successful verification

## Troubleshooting

### Common Issues

**Database Connection Error**
```
Error: Cannot connect to database at localhost:3307
```
- Verify Docker is running: `docker ps`
- Check MySQL logs: `docker logs <container-id>`
- Ensure port 3307 is available

**Port Already in Use**
```
Error: Address already in use: bind
```
- Find process using port 8080: `netstat -ano | findstr :8080` (Windows)
- Kill the process or change port in `application.properties`

**CORS Error in Frontend**
```
Access to XMLHttpRequest blocked by CORS policy
```
- Check `SecurityConfig.java` for CORS configuration
- Verify frontend URL matches `allowedOrigins`

**2FA OTP Not Received**
- Verify SMTP configuration in `application.properties`
- Check email spam folder
- Ensure email address is valid in user profile

**Login Issues**
- Clear browser cache and cookies
- Verify user account is approved (check audit logs)
- Confirm JWT secret matches between application restarts

**Build Failures**
```bash
# Clean and rebuild
mvn clean install -DskipTests -U

# Check Java version
java -version  # Should be 17+
```

### Getting Help

1. Check application logs: `tail -f logs/application.log`
2. Review endpoint documentation in Swagger UI: `http://localhost:8080/swagger-ui.html`
3. Check the audit logs in the Admin dashboard
4. Review backend console output for error messages

## License

This project is proprietary and confidential. All rights reserved.

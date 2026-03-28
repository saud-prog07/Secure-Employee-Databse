# Secure Employee Management System (EMS)

A premium, enterprise-grade Spring Boot & React application for secure organizational management. Features full RBAC (Role-Based Access Control), soft-delete restoration, and administrative auditing.

---

## 🚀 Pro-Features & Logic

- **Premium Design System**: Fully overhauled UI with a modern Slate/Indigo theme, smooth micro-animations, and responsive layouts.
- **Soft-Delete & Recovery**: Deleted records are logically archived. Admins can view and restore them at any time from the dashboard.
- **Full Audit Trail**: Every sensitive action (Approvals, Deletions, Restores, Logins) is tracked in a dedicated audit log with timestamps and user attribution.
- **Multi-Level Security**:
    - **JWT Authentication**: Stateless token-based security.
    - **Granular RBAC**: Specific permissions for `ADMIN` and `HR` roles.
    - **Admin Approval Workflow**: New HR registrations and employee records can be held for admin verification.
- **User Self-Service**: Dedicated **Profile** module for users to manage their own security credentials.
- **Admin Command Center**: Unified **User Management** portal to oversee system access and approve HR staff.

---

## 🛠 Tech Stack

### Backend
- **Core**: Java 17 / Spring Boot 3.2
- **Security**: Spring Security 6 & JJWT
- **Persistence**: MySQL 8.0 / Hibernate JPA
- **API Docs**: Swagger / OpenAPI 3.0

### Frontend
- **Framework**: React.js / React Router 6
- **Styling**: Vanilla CSS (Custom Variable-Driven Design System)
- **HTTP Client**: Axios with Interceptors

---

## 🐳 Quick Start (Docker Support)

The system is fully containerized for a zero-configuration setup.

1. **Spin up the Database**:
   ```bash
   docker compose up -d
   ```
   *This starts MySQL 8.0 on port **3307** to avoid local conflicts.*

2. **Run the Backend**:
   ```bash
   mvn clean install -DskipTests
   mvn spring-boot:run
   ```

3. **Run the Frontend**:
   ```bash
   cd frontend
   npm install
   npm start
   ```
   *Access the UI at `http://localhost:3000` (or 3001 if port is taken).*

---

## 🔐 Default Credentials

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `admin` | **ADMIN** | Full system access & audit viewing |
| `hr` | `hr` | **HR** | Standard employee management |

---

## 📊 API Summary

| Endpoint | Method | Role | Description |
| :--- | :--- | :--- | :--- |
| `/api/auth/register` | POST | Public | HR registration request |
| `/api/v1/employees` | GET | Any | List approved employees |
| `/api/v1/employees/search` | GET | Any | Advanced filtered search |
| `/api/v1/employees/{id}/restore` | PUT | ADMIN | Recover a deleted employee |
| `/api/admin/audit` | GET | ADMIN | View system activity history |
| `/api/admin/users` | GET | ADMIN | Manage and approve system users |
| `/api/profile/password` | PUT | Any | Change your own password |

---
*Developed with a focus on Security, Traceability, and User Experience.*

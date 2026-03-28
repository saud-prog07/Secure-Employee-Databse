# Secure Employee Management System (EMS)

A premium, enterprise-grade Spring Boot & React application for secure organizational management. Features full RBAC (Role-Based Access Control), soft-delete restoration, and administrative auditing.

---

## 🚀 Pro-Features & Governance Logic

- **Premium Design System**: Fully overhauled UI with a modern Slate/Indigo theme, smooth micro-animations, and responsive layouts.
- **Bi-Directional Approvals**: 
    - Admins can **Approve** or **Reject** (revert to pending) any employee record.
    - Admins can **Approve** or **Disapprove** any HR/Administrator account.
- **Dynamic Record Editing**: A newly implemented `Edit` suite allows HR and Admins to modify existing employee professional credentials on the fly.
- **Soft-Delete & Archive Recovery**: Deleted records are logically archived. Admins can view and restore them at any time from the dashboard.
- **Hard Delete (Permanent Cleanup)**: Admins can permanently remove user accounts from the database for system maintenance.
- **Safety Overrides**: Built-in safety logic prevents Admins from accidentally deactivating or deleting their own accounts.
- **Full Audit Trail**: Every sensitive action (Approvals, Rejections, Deletions, Restores, Logins) is tracked in a dedicated audit log with timestamps and user attribution.

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
   *Starts MySQL 8.0 on port **3307** to avoid local conflicts.*

2. **Run the Backend & Auto-Seed**:
   ```bash
   mvn clean install -DskipTests
   mvn spring-boot:run
   ```
   *Seeds default `admin`/`admin` and `hr`/`hr` accounts on first launch.*

3. **Run the Frontend**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

---

## 📊 API Governance Summary

| Endpoint | Method | Access | Description |
| :--- | :--- | :--- | :--- |
| `/api/auth/register` | POST | Public | New HR registration request |
| `/api/v1/employees` | GET | Any | List authorized employees |
| `/api/v1/employees/{id}` | PUT | Any | Edit employee professional details |
| `/api/v1/employees/{id}/reject` | PUT | ADMIN | Revert employee approval status |
| `/api/admin/users/{id}/disapprove` | PUT | ADMIN | Revoke system access for a user |
| `/api/admin/audit` | GET | ADMIN | View system activity history |
| `/api/admin/users` | GET | ADMIN | Manage and approve system accounts |

---
*Developed with a focus on Security, Traceability, and User Experience.*

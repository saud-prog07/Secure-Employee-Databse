# Secure Employee Management System API

This **Spring Boot-based REST API** is designed to manage employee records with a **role-based access control and approval workflow**. It provides a clean, documented, and secure foundation for building organizational management tools.

## 💡 Project Overview

This project simulates a real-world employee management workflow that goes beyond basic CRUD operations. It follows a structured process where:
- **HR Users** can create new employee records, which are initially set to a **PENDING** status.
- **Admin Users** then review and approve these records to move them to an **APPROVED** status.

The goal was to build a system that reflects how professional organizations handle sensitive data—ensuring that no record is truly active until it has been verified by the right person.

## 🚀 Key Features

- **Database Authentication**: Secure login and registration APIs using BCrypt password encryption.
- **Role-Based Authorization (RBAC)**: Distinguishes between **ADMIN** and **HR** roles with specific permissions.
- **Employee Workflow**: Support for **PENDING** and **APPROVED** statuses. New hires start as PENDING until approved by an administrator.
- **Advanced Filtering**: The default employee list only returns `APPROVED` records to ensure data integrity.
- **Full CRUD Operations**: Standardized endpoints for creating, reading, updating, and deleting employees.
- **Pagination & Sorting**: Efficient data handling with Spring Data JPA.
- **Global Exception Handling**: Returns consistent JSON error formats for all common status codes (401, 403, 404, etc.).
- **Interactive Documentation**: Integrated **Swagger UI** (OpenAPI 3) for live testing.

## 📖 API Documentation

The interactive Swagger UI is available at:
`http://localhost:8080/swagger-ui.html`

> [!TIP]
> Use the **"Authorize"** button in Swagger UI to provide your credentials. You can use the default accounts listed below to start testing immediately.

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.2.4, Java 17
- **Database**: MySQL 8.0
- **Security**: Spring Security 6 (Basic Auth)
- **Persistance**: Spring Data JPA (Hibernate 6)
- **Utilities**: Lombok, SLF4J (Logging), Jakarta Validation

## 📋 Essential API Endpoints

| Method | Endpoint | Description | Role Required |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/register` | Register a new user | Public |
| **POST** | `/api/auth/login` | Log in and receive user data | Public |
| **GET** | `/api/v1/employees` | Get all **APPROVED** employees | Public |
| **POST** | `/api/v1/employees` | Create a new employee (**PENDING**) | HR / ADMIN |
| **PUT** | `/api/v1/employees/{id}/approve`| Approve a pending employee | ADMIN |
| **DELETE**| `/api/v1/employees/{id}`| Remove an employee | ADMIN |

## ⚙️ Configuration & Run

### Database Setup
1. Create a database named `employee_db` in your MySQL instance.
2. Update `src/main/resources/application.properties` with your MySQL `username` and `password`.

### Execution
```bash
mvn clean spring-boot:run
```

## 🔒 Default Credentials (Auto-Seeded)

The database is automatically initialized with the following credentials if empty:

| Username | Password | Role |
| :--- | :--- | :--- |
| `admin` | `admin` | **ADMIN** (Full access: Approve, Delete, etc.) |
| `hr` | `hr` | **HR** (Access to Create Employees) |

## 🎯 What I Learned

During the development of this project, I focused on several key architectural and security concepts:
- **Role-Based Access Control**: Implementing fine-grained security rules using **Spring Security** to restrict specific actions based on user roles.
- **Real-World Workflows**: Moving beyond simple CRUD by designing a multi-step approval process (**PENDING → APPROVED**) to mirror actual business requirements.
- **Database Integration**: Seamlessly connecting a Spring Boot application to **MySQL** using **JPA** for persistent, reliable data storage.
- **Error Handling & Validation**: Building a professional API that provides clear, helpful feedback through **Global Exception Handling** and robust input validation.

---
**Production Note**: This project utilizes a custom `AuthenticationEntryPoint` to prevent default browser login popups, providing a seamless experience for modern API clients.

# Secure Employee Management System API

A robust Spring Boot REST API designed for managing organizational employee records. The system features a built-in Role-Based Access Control (RBAC) mechanism and a multi-step approval workflow to ensure data integrity and security.

## Core Features

- **JWT Authentication**: Secure user registration and login with stateless token-based authentication.
- **Role-Based Authorization**: Granular access control for ADMIN and HR roles using Spring Security.
- **Employee CRUD Operations**: Full management of employee records with validation.
- **Approval System**: Administration review process where pending records must be approved by an ADMIN.
- **Dynamic Filtering**: Automated filtering to ensure only APPROVED employees are visible in standard listings.
- **Pagination and Sorting**: Efficient data retrieval with built-in support for large datasets.
- **Global Exception Handling**: Standardized error responses across all API endpoints.
- **Database Integration**: Reliable persistence using MySQL and Hibernate/JPA.
- **Swagger UI Support**: Interactive API documentation for seamless testing and integration.

## API Endpoints

| Endpoint | Method | Role Required | Description |
| :--- | :--- | :--- | :--- |
| `/api/auth/register` | POST | Public | Register a new user (ADMIN or HR) |
| `/api/auth/login` | POST | Public | Authenticate and receive a JWT token |
| `/api/v1/employees` | GET | HR, ADMIN | Retrieve a paginated list of approved employees |
| `/api/v1/employees` | POST | HR, ADMIN | Create a new employee (ADMIN-created are auto-approved) |
| `/api/v1/employees/{id}/approve` | PUT | ADMIN | Approve a pending employee record |
| `/api/v1/employees/{id}` | DELETE | ADMIN | Perform a soft-delete on an employee record |

## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3
- **Security**: Spring Security & JWT
- **Database**: MySQL 8.0
- **Persistence**: Spring Data JPA / Hibernate
- **Build Tool**: Maven

## How to Run

1. **Configure Database**: Update `src/main/resources/application.properties` with your MySQL credentials.
2. **Build Project**:
   ```bash
   mvn clean install
   ```
3. **Start Application**:
   ```bash
   mvn spring-boot:run
   ```
4. **Access API**: The server will start locally at `http://localhost:8080`.

## Default Credentials

The system automatically seeds the following credentials for testing:

- **Username**: `admin` / **Password**: `admin` (Role: **ADMIN**)
- **Username**: `hr` / **Password**: `hr` (Role: **HR**)

## Testing with Postman

1. **Authentication**: Call the `/api/auth/login` endpoint with the default credentials to receive an `accessToken`.
2. **Authorization**: Copy the token and add it to your Postman request under the **Auth** tab as a **Bearer Token**.
3. **Requests**: Perform requests to protected `/api/v1/employees` endpoints. Non-admin users attempting administration tasks will receive a `403 Forbidden` response.

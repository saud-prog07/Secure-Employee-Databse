# Secure Employee Management UI

This is the React-based frontend application for the Secure Employee Management System. It provides a clean, professional interface for administrators and HR staff to manage employee records securely.

## Core Features

- **JWT-Protected Dashboards**: Routes are guarded to ensure only authenticated users can access sensitive records.
- **Role-Based UI Rendering**: 
  - **ADMIN**: Access to full employee lifecycle management, including record approval.
  - **HR**: Capability to view and submit new employee records.
- **Stateless Session Management**: Leverages browser `localStorage` to persist JWT tokens and user roles safely.
- **Automated API Security**: A centralized Axios interceptor automatically injects Bearer tokens into every request header.

## Project Structure

```text
src/
├── pages/
│   ├── Login.js        # Authentication and session initialization
│   ├── Dashboard.js    # Data grid with role-based action filters
│   └── AddEmployee.js  # Submission form for new records
├── services/
│   └── api.js          # Secured Axios instance with request/response interceptors
└── App.js              # Application routing and global state logic
```

## Getting Started

### Prerequisites

- **Node.js**: v17 or higher
- **Backend Service**: Ensure the Spring Boot backend is running at `http://localhost:8080`.

### Setup and Launch

1. **Install Dependencies**:
   ```bash
   npm install
   ```

2. **Run Development Server**:
   ```bash
   npm start
   ```

3. **Access the Application**:
   Navigate to [http://localhost:3000](http://localhost:3000) in your browser.

## Key Developer Commands

- `npm start`: Runs the app in development mode.
- `npm run build`: Bundles the app for production in the `build` folder.
- `npm test`: Launches the test runner in interactive mode.

## Troubleshooting

- **CORS Errors**: If the UI cannot reach the backend, verify that your Spring Boot controllers are configured with `@CrossOrigin`.
- **Session Timeout**: If you are redirected to Login unexpectedly, your JWT token may have expired. Simply log in again to refresh your session.

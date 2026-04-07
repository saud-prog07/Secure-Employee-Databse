# PRODUCTION SECURITY SETUP GUIDE

## Secure First-Time Setup for Production

This guide explains how to securely initialize the Employee Management System for production without using default credentials.

---

## 🔒 Phase 1: Database Setup (Before Starting Application)

### 1. Create MySQL Database

```bash
# Connect to MySQL as root
mysql -u root -p

# Run the migration script
SOURCE db-migration-master.sql;

# Verify tables were created
SHOW TABLES;
SHOW TABLES FROM employee_db;

EXIT;
```

### 2. Create Initial MySQL User (Optional but Recommended)

```sql
-- Create dedicated app user (do NOT use root in production)
CREATE USER 'ems_user'@'localhost' IDENTIFIED BY 'VERY_STRONG_PASSWORD_HERE';

-- Grant necessary privileges
GRANT ALL PRIVILEGES ON employee_db.* TO 'ems_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 🔐 Phase 2: Environment Configuration

### 1. Set Environment Variables for Backend

Before running the application, configure these environment variables:

**Linux/Mac (.env file or export):**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USERNAME=ems_user           # Use dedicated user, NOT root
export DB_PASSWORD=STRONG_DB_PASSWORD
export DB_NAME=employee_db

export JWT_SECRET=$(openssl rand -base64 32)  # Generate strong random key
export JWT_EXPIRATION_MS=86400000              # 24 hours

export SEED_DEFAULT_USERS=false                # CRITICAL: Keep false in production!
export APP_ENV=production                      # Set to production

export SMTP_HOST=smtp.yourdomain.com
export SMTP_PORT=587
export EMAIL_USERNAME=noreply@yourdomain.com
export EMAIL_PASSWORD=SMTP_APP_PASSWORD
```

**Windows PowerShell:**
```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_USERNAME = "ems_user"
$env:DB_PASSWORD = "STRONG_DB_PASSWORD"
$env:DB_NAME = "employee_db"

$env:JWT_SECRET = "USE_OPENSSL_RAND_TO_GENERATE_THIS"
$env:JWT_EXPIRATION_MS = "86400000"

$env:SEED_DEFAULT_USERS = "false"
$env:APP_ENV = "production"

$env:SMTP_HOST = "smtp.yourdomain.com"
$env:SMTP_PORT = "587"
$env:EMAIL_USERNAME = "noreply@yourdomain.com"
$env:EMAIL_PASSWORD = "SMTP_APP_PASSWORD"
```

### Generate Secure JWT Secret

```bash
# macOS/Linux
openssl rand -base64 32

# Windows PowerShell
[System.Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))

# Keep output for JWT_SECRET env var
```

---

## 👤 Phase 3: Create First Admin User (Secure Method)

Since `SEED_DEFAULT_USERS=false`, you must manually create the first admin user.

### Option A: Direct Database Insertion (Recommended)

```bash
# Generate password hash using this utility or similar
# Use the PasswordValidator requirements:
# - 12+ characters
# - 1+ uppercase (A-Z)
# - 1+ lowercase (a-z)
# - 1+ digit (0-9)
# - 1+ special char (!@#$%^&*)

# Example strong password: MyDogRuns#2024Spring
```

**Steps:**

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```
   (It will fail to start, but that's okay - we're about to add the admin user)

2. **In another terminal, connect to MySQL and insert admin:**
   ```sql
   mysql -u ems_user -p employee_db
   
   -- Example using a bcrypt hash
   -- Hash for "MyDogRuns#2024Spring" (you need to generate actual hash)
   INSERT INTO users (
     username, 
     email, 
     password, 
     role, 
     approved, 
     deleted, 
     two_factor_enabled,
     created_at
   ) VALUES (
     'admin',
     'admin@yourdomain.com',
     '$2a$10$...',  -- BCRYPT HASH HERE (from step below)
     'ADMIN',
     true,
     false,
     false,
     NOW()
   );
   ```

3. **Generate Bcrypt Hash for Your Chosen Password:**

   Create a temporary Java file to generate the hash:
   
   ```java
   // PasswordHashGenerator.java
   import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
   
   public class PasswordHashGenerator {
       public static void main(String[] args) {
           BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
           String password = "MyDogRuns#2024Spring";  // Change this!
           String hash = encoder.encode(password);
           System.out.println("Password: " + password);
           System.out.println("Bcrypt Hash: " + hash);
       }
   }
   ```
   
   Or use the Spring Boot app to generate:
   ```bash
   # Start app with temporary SEED_DEFAULT_USERS=true
   export SEED_DEFAULT_USERS=true
   mvn spring-boot:run
   # Kill after startup
   
   # Then check the password hash in logs or query the database:
   SELECT username, password FROM users WHERE username='admin';
   ```

### Option B: API Endpoint (After Admin Exists)

Once you have at least one admin:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newhradmin",
    "email": "hr@yourdomain.com",
    "password": "MyPasswordIs#Strong2024",
    "role": "HR"
  }'
```

---

## ⚠️ Critical Security Checklist Before Production

- [ ] **SEED_DEFAULT_USERS=false** - No default admin/hr credentials exposed
- [ ] **APP_ENV=production** - Application runs in production mode
- [ ] **JWT_SECRET** - Set to strong 256+ bit random value
- [ ] **Database Credentials** - Using dedicated non-root user
- [ ] **SSL/HTTPS** - Enforced in production
- [ ] **All Passwords** - Meet strength requirements (12 chars, upper, lower, digit, special)
- [ ] **No Hardcoded Values** - All secrets in environment variables
- [ ] **Backup Strategy** - Database backups configured
- [ ] **Monitoring** - Logging and alerting configured
- [ ] **Firewall Rules** - Port 8080 not exposed to internet (use reverse proxy)

---

## 🚀 Starting Production Application

```bash
# Set all environment variables first (see Phase 2)

# Then start application
mvn spring-boot:run

# Or if packaged as JAR:
java -jar employee-management-system-0.0.1-SNAPSHOT.jar
```

### Expected Startup Output

```
===== APPLICATION ENVIRONMENT: PRODUCTION =====
===== DEFAULT USER SEEDING: DISABLED =====
Verifying MySQL database connection...
MySQL Connection SUCCESSFUL. Current stats: 1 users, 0 employees found.
```

You should **NOT** see the warning about default credentials.

---

## 🔄 Password Change for Existing Users

Users can change their password via the `/api/auth/change-password` endpoint:

```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "current_password",
    "newPassword": "NewPassword#2024Spring"
  }'
```

---

## 📋 Password Requirements

All new passwords must:
- Be **at least 12 characters** long
- Contain **at least 1 UPPERCASE** letter (A-Z)
- Contain **at least 1 lowercase** letter (a-z)
- Contain **at least 1 digit** (0-9)
- Contain **at least 1 special character** (!@#$%^&*()_+-=[]{};':\"\\|,.<>/?))

**Example Valid Passwords:**
- `Admin@Password2024`
- `MyDogRuns#2024Spring`
- `SecureP@ssw0rd!Admin`
- `ProducTion#123Secure`

**Example Invalid Passwords:**
- `admin123` (too short, no special char)
- `AdminPassword` (no digit, no special char)
- `admin@123` (no uppercase)
- `ADMIN@123` (no lowercase)

---

## 🆘 Troubleshooting

### "User not found" error at startup

**Problem:** Application can't find admin user  
**Solution:** 
1. Manually insert admin user (Option A above)
2. OR set `SEED_DEFAULT_USERS=true` temporarily for first run
3. Verify `SEED_DEFAULT_USERS=false` is set before production

### "Connection refused" to database

**Problem:** Database not accessible  
**Solution:**
1. Verify MySQL is running
2. Check DB_HOST, DB_PORT, DB_USERNAME credentials
3. Verify firewall allows connection on DB_PORT

### JWT token expired errors

**Problem:** Users being logged out frequently  
**Solution:**
1. Check JWT_EXPIRATION_MS value (default 86400000 = 24 hours)
2. Adjust if shorter sessions needed

---

## 📞 Security Contact

For security issues, **DO NOT** create public GitHub issues. Report to: `security@yourdomain.com`

Version: 1.0  
Last Updated: April 2026

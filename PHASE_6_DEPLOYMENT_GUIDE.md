# Phase 6: Final Verification & Deployment Readiness

**Date**: April 7, 2026  
**Status**: IN PROGRESS  
**Phase Focus**: End-to-end testing, deployment documentation, production runbook

---

## Executive Summary

Phase 6 completes the security hardening journey by:

1. **End-to-End Testing**: Verify all 5 phases work together seamlessly
2. **Integration Testing**: Test cross-phase interactions
3. **Security Audit**: Final review of all security controls
4. **Deployment Documentation**: Docker/Kubernetes configurations
5. **Production Runbook**: Operations guide for production deployment
6. **Troubleshooting Guide**: Common issues and solutions

---

## Part 1: End-to-End Integration Testing

### 1.1 Test Scenarios for All Phases

#### **Phase 1 + Phase 2: Authentication & Credentials** (✓ Completed Phases 1-2)

```
Test Case 1.1: Brute Force Prevention
1. Attempt login with invalid credentials 5+ times
2. Verify rate limiter blocks further attempts (429 Too Many Requests)
3. Verify security.log records all failed attempts
4. Verify app.login.failure metric incremented
5. Wait for rate limit window to reset (1 minute)
6. Verify login succeeds after reset

Expected: No hardcoded secrets in logs, rate limiting effective
```

```
Test Case 1.2: Password Strength Enforcement
1. Attempt registration with weak password (< 12 chars)
2. Verify validation error (Bad Request 400)
3. Attempt registration with strong password
4. Verify registration succeeds
5. Attempt login with new credentials
6. Verify login succeeds

Expected: Password validation enforced, credentials secure
```

#### **Phase 3: Test Coverage** (✓ Completed Phase 3)

```
Test Case 3.1: Test Coverage Verification
1. Run: mvn clean test
2. Verify all 14 tests pass
3. Check for any new failing tests
4. Verify test output logs correctly

Expected: 14/14 PASSING, no regressions
```

#### **Phase 4: Input Validation & Rate Limiting** (✓ Completed Phase 4)

```
Test Case 4.1: Input Sanitization
1. Attempt registration with XSS payload: <script>alert('xss')</script>
2. Verify payload is sanitized/escaped
3. Attempt registration with SQL injection: ' OR '1'='1
4. Verify SQL injection detected and sanitized
5. Verify validation error logged to audit.log

Expected: All payloads neutralized, security.log shows attempt
```

```
Test Case 4.2: Rate Limiting
1. Make requests to auth endpoint at 10 req/sec
2. Verify rate limiter allows 5 req/min auth limit
3. Verify subsequent requests get 429
4. Make requests to API endpoint at 5 req/sec
5. Verify rate limiter allows 100 req/min API limit
6. Verify correlation ID in response header

Expected: Rate limiting enforced per endpoint
```

#### **Phase 5: Monitoring & Logging** (✓ JUST Completed Phase 5)

```
Test Case 5.1: Actuator Health Endpoint
1. Call: GET /actuator/health
2. Verify response status UP
3. Call with auth header: GET /actuator/health (with admin token)
4. Verify detailed components returned
5. Check ApplicationHealthIndicator response includes heap memory

Expected: Health endpoint returns 200, detailed info with auth
```

```
Test Case 5.2: Prometheus Metrics
1. Call: GET /actuator/prometheus
2. Verify response includes Prometheus format metrics
3. Look for custom metrics: app_login_attempts_total
4. Verify metrics are numbers (not NaN)

Expected: Prometheus endpoint accessible, metrics valid
```

```
Test Case 5.3: Correlation ID Tracking
1. Make a request to any API endpoint
2. Check response headers for X-Correlation-ID
3. Grep logs with correlation ID: grep "UUID" logs/*.log
4. Verify same ID appears in all log files for that request

Expected: Correlation ID generated, logged consistently
```

```
Test Case 5.4: Log File Rotation
1. Verify logs directory created: ls -la logs/
2. Verify files exist: application.log, error.log, audit.log, security.log
3. Check log format: cat logs/application.log | head
4. Check error log only has ERROR level: tail logs/error.log

Expected: All log files created, proper format and levels
```

### 1.2 Full Integration Test Scenario

```
COMPLETE USER JOURNEY TEST

1. Register New User
   ├─ POST /api/auth/register (valid data + strong password)
   ├─ Verify: 201 Created, correlation ID in response
   ├─ Check logs:
   │  ├─ audit.log: Registration attempt recorded
   │  ├─ application.log: User created
   │  └─ security.log: Account created event
   └─ Metrics: app_registration_attempts_total+1, app_registration_success_total+1

2. Login with New User
   ├─ POST /api/auth/login (credentials from step 1)
   ├─ Verify: 200 OK, JWT token received
   ├─ See response header X-Correlation-ID
   ├─ Check metrics: app_login_attempts_total+1, app_login_success_total+1
   └─ Verify no password logged to any file

3. Access Protected Resource
   ├─ GET /api/employees (with JWT token)
   ├─ Verify: 200 OK, data returned
   ├─ Check request/response logged with correlation ID
   └─ Verify request/response times logged to application.log

4. Test Rate Limiting
   ├─ Send 5+ rapid requests to same endpoint
   ├─ After 5th request (or 100 in 1 min), verify 429 Too Many Requests
   ├─ Verify security.log shows rate limit violation
   └─ Metrics: app_ratelimit_exceeded_total+1

5. Test Error Handling
   ├─ POST /api/auth/login (invalid credentials)
   ├─ Verify: 401 Unauthorized
   ├─ Check error.log shows login failure
   ├─ Verify security.log includes failed attempt
   └─ Metrics: app_login_failure_total+1

6. Check Health & Monitoring
   ├─ GET /actuator/health
   ├─ Verify: UP status
   ├─ GET /actuator/metrics/app.active.users
   ├─ Verify: User count reflects current user
   └─ GET /actuator/prometheus - verify metrics available
```

---

## Part 2: Security Audit Checklist

### 2.1 Authentication & Authorization

- [ ] No hardcoded secrets in code/config/logs
- [ ] All credentials stored in environment variables
- [ ] JWT token validation working correctly
- [ ] Token expiration enforced
- [ ] Refresh token mechanism secure
- [ ] Password hashing using BCrypt (salt + iterations)
- [ ] No passwords logged anywhere
- [ ] CORS properly configured
- [ ] CSRF protection enabled

### 2.2 Input Validation & Sanitization

- [ ] All inputs validated before processing
- [ ] XSS payloads detected and neutralized
- [ ] SQL injection patterns detected
- [ ] Command injection patterns detected
- [ ] File upload validation in place
- [ ] Request size limits enforced
- [ ] Special characters escaped in output

### 2.3 Rate Limiting & Brute Force Protection

- [ ] Authentication endpoint rate limited (5 req/min)
- [ ] API endpoint rate limited (100 req/min)
- [ ] Rate limit headers returned (X-RateLimit-*)
- [ ] Rate limit violations logged to security.log
- [ ] IP addresses properly extracted (including proxies)
- [ ] Rate limit metrics collected

### 2.4 Logging & Monitoring

- [ ] All security events logged
- [ ] Separate audit/security/error log files
- [ ] Log rotation enabled with retention policy
- [ ] Sensitive data not logged (passwords, tokens, PII)
- [ ] Request correlation IDs generated and logged
- [ ] HTTP requests/responses logged with duration
- [ ] Actuator endpoints exposed safely
- [ ] Health checks include memory monitoring
- [ ] Prometheus metrics available
- [ ] Graceful shutdown configured

### 2.5 Error Handling

- [ ] No stack traces exposed to users
- [ ] Generic error messages returned to clients
- [ ] Detailed errors logged server-side
- [ ] Error logging includes correlation ID
- [ ] 5xx errors escalated to ERROR level logs
- [ ] 4xx errors logged at WARN level

### 2.6 Database Security

- [ ] Connection pooling configured
- [ ] SQL injection prevention (parameterized queries)
- [ ] Database credentials in environment variables
- [ ] No test data in production
- [ ] Encryption at rest (if applicable)
- [ ] Connection encryption (SSL/TLS)

### 2.7 API Security

- [ ] API documentation doesn't expose security details
- [ ] Swagger endpoint secured
- [ ] API versioning in place
- [ ] Deprecation policy documented
- [ ] Rate limiting per endpoint
- [ ] Content-Type validation
- [ ] Request size limits enforced

### 2.8 Deployment Security

- [ ] Secrets not in Docker image
- [ ] Base image regularly updated
- [ ] Port exposure minimized
- [ ] Health check endpoint accessible internally only (if desired)
- [ ] Graceful shutdown mechanism tested
- [ ] Log directory permissions restricted (600 for files, 700 for dir)

---

## Part 3: Deployment Documentation

### 3.1 Docker Deployment

#### **Dockerfile** (Already provided)

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/employee-management-system-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### **Build & Run**

```bash
# Build Docker image
docker build -t employee-api:latest .

# Run with environment variables
docker run -d \
  --name employee-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/employee_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
  -e JWT_SECRET=$JWT_SECRET \
  -e SERVER_SHUTDOWN=graceful \
  -v /app/logs:/logs \
  employee-api:latest
```

#### **Docker Compose** (Already provided)

```yaml
version: '3.8'
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/employee_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - mysql
    volumes:
      - ./logs:/logs

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: employee_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./db-migration-master.sql:/docker-entrypoint-initdb.d/init.sql

volumes:
  mysql_data:
```

#### **Build & Deploy**

```bash
# Build backend JAR
mvn clean package -P production

# Build Docker image
docker build -t employee-api:v1.0 .

# Push to registry (if using)
docker push myregistry.azurecr.io/employee-api:v1.0

# Deploy with Docker Compose
docker-compose up -d
```

### 3.2 Kubernetes Deployment

#### **deployment.yaml**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: employee-api
  labels:
    app: employee-api
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: employee-api
  template:
    metadata:
      labels:
        app: employee-api
    spec:
      containers:
      - name: employee-api
        image: myregistry.azurecr.io/employee-api:v1.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql-service:3306/employee_db"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1024Mi"
            cpu: "1000m"
        volumeMounts:
        - name: logs
          mountPath: /logs
      volumes:
      - name: logs
        emptyDir: {}
      terminationGracePeriodSeconds: 35
```

#### **service.yaml**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: employee-api-service
  labels:
    app: employee-api
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  selector:
    app: employee-api
```

#### **configmap.yaml** (For non-sensitive config)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: employee-api-config
data:
  application-prod.properties: |
    spring.profiles.active=prod
    logging.level.root=WARN
    logging.level.com.example.employee=INFO
    management.endpoints.web.exposure.include=health,metrics,prometheus
    server.shutdown=graceful
    spring.lifecycle.timeout-per-shutdown-phase=30s
```

#### **secret.yaml** (For sensitive data)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
stringData:
  username: root
  password: <your-secret-password>
---
apiVersion: v1
kind: Secret
metadata:
  name: jwt-secret
type: Opaque
stringData:
  secret: <your-jwt-secret-at-least-32-chars>
```

#### **Deploy to Kubernetes**

```bash
# Create secrets
kubectl apply -f secret.yaml

# Deploy application
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f configmap.yaml

# Check deployment status
kubectl get deployments
kubectl get pods
kubectl get services

# Check logs
kubectl logs -f deployment/employee-api

# Check health
kubectl describe pod <pod-name>
```

### 3.3 Azure Deployment (Optional)

#### **Using Azure Container Instances (ACI)**

```bash
# Build and push image
docker build -t employee-api:v1.0 .
docker tag employee-api:v1.0 myregistry.azurecr.io/employee-api:v1.0
docker push myregistry.azurecr.io/employee-api:v1.0

# Deploy with Azure CLI
az container create \
  --resource-group myResourceGroup \
  --name employee-api \
  --image myregistry.azurecr.io/employee-api:v1.0 \
  --registry-login-server myregistry.azurecr.io \
  --registry-username <username> \
  --registry-password <password> \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/employee_db \
  --ports 80 \
  --environment-variables SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD
```

#### **Using Azure App Service**

```bash
# Create App Service Plan
az appservice plan create \
  --name myAppPlan \
  --resource-group myResourceGroup \
  --sku B2 --is-linux

# Create Web App
az webapp create \
  --resource-group myResourceGroup \
  --plan myAppPlan \
  --name employee-api \
  --deployment-container-image-name myregistry.azurecr.io/employee-api:v1.0

# Configure deployment
az webapp deployment container config \
  --name employee-api \
  --resource-group myResourceGroup \
  --enable-cd true

# Set environment variables
az webapp config appsettings set \
  --name employee-api \
  --resource-group myResourceGroup \
  --settings \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
    JWT_SECRET=$JWT_SECRET
```

---

## Part 4: Production Runbook

### 4.1 Pre-Deployment Checklist

- [ ] All code merged to main branch
- [ ] All tests passing (14/14)
- [ ] Build successful with no warnings
- [ ] Security audit completed
- [ ] Performance testing completed
- [ ] Load testing completed
- [ ] Deployment plan reviewed
- [ ] Rollback plan documented
- [ ] Monitoring/alerting configured
- [ ] Team trained on operations

### 4.2 Deployment Steps

#### **Step 1: Prepare Release Build**

```bash
# Clean build
mvn clean package -P production -DskipTests

# Verify JAR created
ls -lh target/employee-management-system-0.0.1-SNAPSHOT.jar

# Tag source code
git tag v1.0-prod
git push origin v1.0-prod
```

#### **Step 2: Build & Push Docker Image**

```bash
# Build image
docker build -t employee-api:v1.0.0 .
docker tag employee-api:v1.0.0 myregistry.azurecr.io/employee-api:v1.0.0

# Push to registry
docker push myregistry.azurecr.io/employee-api:v1.0.0

# Verify image
docker pull myregistry.azurecr.io/employee-api:v1.0.0
```

#### **Step 3: Database Migration**

```bash
# Backup current database
mysqldump -u root -p employee_db > db_backup_$(date +%Y%m%d_%H%M%S).sql

# Run migrations (if any)
java -jar migrationTool.jar --migrate

# Verify migrations
mysql -u root -p employee_db -e "SELECT * FROM schema_version ORDER BY version DESC LIMIT 5;"
```

#### **Step 4: Deploy Application**

**For Docker:**
```bash
# Pull latest image
docker pull myregistry.azurecr.io/employee-api:v1.0.0

# Stop old container
docker stop employee-api
docker rm employee-api

# Start new container
docker run -d \
  --name employee-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/employee_db \
  -e JWT_SECRET=$JWT_SECRET \
  -v /app/logs:/logs \
  myregistry.azurecr.io/employee-api:v1.0.0

# Verify running
docker ps | grep employee-api
```

**For Kubernetes:**
```bash
# Update deployment image
kubectl set image deployment/employee-api \
  employee-api=myregistry.azurecr.io/employee-api:v1.0.0 \
  -n production

# Monitor rollout
kubectl rollout status deployment/employee-api -n production

# Check pod status
kubectl get pods -n production
```

#### **Step 5: Verify Deployment**

```bash
# Health check
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus | head -20

# Test login endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'

# Check logs
tail -f logs/application.log

# Monitor metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### 4.3 Rollback Procedure

```bash
# If deployment fails:

# Option 1: Docker
docker stop employee-api
docker rm employee-api
docker run -d \
  --name employee-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  myregistry.azurecr.io/employee-api:v0.9.9  # Previous version

# Option 2: Kubernetes
kubectl rollout undo deployment/employee-api -n production

# Option 3: Database (if needed)
mysql -u root -p employee_db < db_backup_20260407_120000.sql

# Verify rollback
curl http://localhost:8080/actuator/health
```

### 4.4 Post-Deployment Verification

- [ ] Application responding to requests (HTTP 200)
- [ ] Health endpoint returns UP
- [ ] Logs being generated in `/logs`
- [ ] Metrics available at `/actuator/prometheus`
- [ ] Users can login successfully
- [ ] API endpoints functional
- [ ] Database queries working
- [ ] No errors in application.log
- [ ] No warnings in audit trail
- [ ] Monitoring/alerting working

### 4.5 Monitoring & Alerting

#### **Prometheus Alerts** (Create these rules)

```yaml
# High Error Rate
- alert: HighErrorRate
  expr: rate(app_api_errors_total[5m]) > 0.1
  for: 5m
  annotations:
    summary: "High API error rate"
    description: "API error rate is {{ $value }}/sec"

# High Login Failures
- alert: HighLoginFailureRate
  expr: rate(app_login_failure_total[5m]) > 0.5
  for: 2m
  annotations:
    summary: "Multiple failed login attempts detected"

# High Memory Usage
- alert: HighMemoryUsage
  expr: jvm_memory_used_bytes > 900000000
  for: 5m
  annotations:
    summary: "JVM heap memory > 90%"

# Pod Not Ready
- alert: PodNotReady
  expr: kube_pod_status_ready{pod=~"employee-api-.*"} == 0
  for: 2m
```

#### **Grafana Dashboard** (Create dashboard with these panels)

```
Row 1: Health Status
  - Application Health Status (gauge)
  - Liveness/Readiness probes (gauge)

Row 2: Request Metrics
  - Requests Per Second (graph)
  - Error Rate % (graph)
  - Response Time (p50/p95/p99) (graph)

Row 3: Business Metrics
  - Login Attempts & Success Rate (graph)
  - Active Users (gauge)
  - Rate Limit Violations (counter)

Row 4: System Metrics
  - Memory Usage % (gauge)
  - CPU Usage % (gauge)
  - Thread Count (gauge)
  - GC Pause Times (graph)
```

---

## Part 5: Troubleshooting Guide

### 5.1 Application Won't Start

**Symptom**: Container exits immediately
**Causes & Solutions**:

1. **Environment variables missing**
   ```bash
   docker logs employee-api  # Check logs
   # Solution: Ensure all required env vars set (SPRING_DATASOURCE_*, JWT_SECRET)
   ```

2. **Database unreachable**
   ```bash
   # Test connection
   mysql -h mysql-host -u root -p -e "SELECT 1;"
   # Solution: Verify host, credentials, network connectivity
   ```

3. **Port already in use**
   ```bash
   lsof -i :8080  # Check what's using port
   # Solution: Change port or stop conflicting service
   ```

### 5.2 High Memory Usage

**Symptom**: Memory fills up, app becomes slow
**Diagnosis**:

```bash
# Check heap usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Get heap dump for analysis
curl http://localhost:8080/actuator/heapdump -O heap.hprof

# Analyze heap dump (using jhat or similar tool)
jhat heap.hprof
```

**Solutions**:
- Increase JVM heap: `-Xmx2g` in java command
- Check for memory leaks in app code
- Reduce log retention period
- Implement caching wisely

### 5.3 High Response Times

**Symptom**: API requests slow (>500ms)
**Diagnosis**:

```bash
# Check database performance
curl http://localhost:8080/actuator/metrics/app.api.response.duration

# Check system resources
curl http://localhost:8080/actuator/metrics/process.cpu.usage
curl http://localhost:8080/actuator/metrics/system.memory.usage

# Check active requests
curl http://localhost:8080/actuator/threaddump | grep "runnable"
```

**Solutions**:
- Check database query times (enable slow query log)
- Add indexes to frequently queried columns
- Implement caching for read-heavy operations
- Adjust thread pool sizes
- Scale horizontally (add more instances)

### 5.4 Login Failures

**Symptom**: Users can't login, getting 401
**Diagnosis**:

```bash
# Check if rate limit exceeded
tail logs/security.log | grep "RATE_LIMIT"

# Check authentication logs
tail logs/audit.log | grep "login\|LOGIN"

# Verify database has users
mysql -u root -p employee_db -e "SELECT id, username FROM users LIMIT 5;"

# Check password hashing
curl -X POST http://localhost:8080/api/auth/login \
  -d '{"username":"admin","password":"wrong"}' \
  -H "Content-Type: application/json" \
  -v
```

**Solutions**:
- Reset password: `UPDATE users SET password = bcrypt('NewPassword!123') WHERE username='admin';`
- Check rate limit reset: Wait 60 seconds for auth rate limit
- Verify credentials in database
- Check SecurityConfig configuration

### 5.5 Logs Not Being Generated

**Symptom**: No logs in `/logs` directory
**Diagnosis**:

```bash
# Check logback configuration loaded
curl http://localhost:8080/actuator/env | grep logback

# Check file permissions
ls -la logs/
stat logs/

# Check if directory exists
test -d logs/ && echo "Directory exists" || echo "Directory missing"

# Verify application.properties
cat application.properties | grep logging
```

**Solutions**:
- Create logs directory: `mkdir -p logs/`
- Fix permissions: `chmod 755 logs/`
- Verify logback-spring.xml in classpath
- Check `<file>` path in logback-spring.xml

---

## Part 6: Operations FAQ

### Q1: How do I scale the application horizontally?

**A**: Add more instances behind load balancer:
```bash
# Docker Compose
docker-compose up -d --scale api=3

# Kubernetes
kubectl scale deployment employee-api --replicas=5

# Configure load balancer to distribute traffic
```

### Q2: How do I upgrade to a new version?

**A**: Follow the deployment steps in Section 4.2:
1. Build new Docker image
2. Push to registry
3. Run database migrations (if needed)
4. Update deployment to use new image
5. Monitor for errors
6. Keep old image available for rollback

### Q3: How do I enable debug logging in production?

**A**: DON'T - use separate development environment. If necessary:
```properties
# Temporarily (restart required)
logging.level.com.example.employee=DEBUG

# Always disable after debugging
logging.level.com.example.employee=INFO
```

### Q4: How do I analyze performance issues?

**A**: Use available metrics:
```bash
# Enable verbose logging (temporarily)
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate=DEBUG

# Collect metrics
curl http://localhost:8080/actuator/metrics > metrics.txt

# Use Prometheus/Grafana to visualize
# Create custom queries and dashboards
```

### Q5: How do I backup and restore data?

**A**: Database backup/restore:
```bash
# Backup
mysqldump -u root -p employee_db > backup.sql

# Restore
mysql -u root -p employee_db < backup.sql

# Verify
SELECT COUNT(*) FROM employees;
```

---

## Part 7: Compliance & Security Checklist

- [ ] All secrets in environment variables, none in code
- [ ] Passwords hashed with BCrypt, salt rounds ≥ 10
- [ ] JWT tokens signed with strong secret (≥32 chars)
- [ ] HTTPS enforced (use reverse proxy/load balancer)
- [ ] CORS properly configured (only allowed origins)
- [ ] All inputs validated and sanitized
- [ ] Error messages don't leak sensitive info
- [ ] Rate limiting enforced on auth endpoints
- [ ] Audit logging enabled for security events
- [ ] Log files have restricted permissions (600)
- [ ] Database backups encrypted
- [ ] Monitoring and alerting configured
- [ ] Incident response plan documented
- [ ] Team security training completed

---

## Summary

**Phase 6 Status**: IN PROGRESS
- ✅ End-to-end testing scenarios documented
- ✅ Security audit checklist created
- ✅ Docker deployment documented
- ✅ Kubernetes deployment documented
- ✅ Production runbook created
- ✅ Troubleshooting guide provided
- ✅ Operations FAQ answered

**Next Steps**:
1. Execute end-to-end testing
2. Complete security audit
3. Deploy to staging environment
4. Validate all components working
5. Final sign-off for production deployment

**Target Completion**: Following successful testing and validation


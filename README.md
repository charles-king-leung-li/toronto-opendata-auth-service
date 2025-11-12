# Toronto Open Data Auth Service

## Overview
Microservice for authentication and authorization in the Toronto Open Data platform.

## Features
- ✅ JWT-based authentication
- ✅ Role-Based Access Control (RBAC)
- ✅ Permission management
- ✅ User registration and login
- ✅ Refresh token support
- ✅ PostgreSQL database
- ✅ Spring Security integration

## Architecture

### Database Schema
```
users
├── id (PK)
├── username (unique)
├── email (unique)
├── password (hashed)
├── first_name
├── last_name
├── enabled
└── timestamps

roles
├── id (PK)
├── name (unique)
└── description

permissions
├── id (PK)
├── name (unique)
├── description
├── resource
└── action

user_roles (many-to-many)
├── user_id (FK)
└── role_id (FK)

role_permissions (many-to-many)
├── role_id (FK)
└── permission_id (FK)
```

## Setup

### 1. Database Setup
```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE toronto_opendata_auth;

-- Exit
\q
```

### 2. Configuration
Copy and configure your local settings:
```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Update with your PostgreSQL password.

### 3. Build and Run
```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

Service runs on port **8082**.

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER"]
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com"
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Authorization: Bearer {refresh_token}
```

### User Management

#### Get All Users (Admin only)
```http
GET /api/users
Authorization: Bearer {token}
```

#### Get User by ID
```http
GET /api/users/{id}
Authorization: Bearer {token}
```

#### Update User
```http
PUT /api/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith"
}
```

#### Delete User (Admin only)
```http
DELETE /api/users/{id}
Authorization: Bearer {token}
```

### Role Management

#### Get All Roles
```http
GET /api/roles
Authorization: Bearer {token}
```

#### Create Role (Admin only)
```http
POST /api/roles
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "MODERATOR",
  "description": "Content moderator role",
  "permissions": ["READ_CONTENT", "UPDATE_CONTENT"]
}
```

#### Assign Role to User (Admin only)
```http
POST /api/users/{userId}/roles/{roleId}
Authorization: Bearer {token}
```

### Permission Management

#### Get All Permissions
```http
GET /api/permissions
Authorization: Bearer {token}
```

#### Create Permission (Admin only)
```http
POST /api/permissions
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "READ_HOTSPOTS",
  "description": "Read cultural hotspots",
  "resource": "hotspots",
  "action": "READ"
}
```

## Default Roles

The service initializes with these default roles:

- **ADMIN**: Full system access
- **USER**: Basic authenticated user
- **GUEST**: Read-only access

## Permission Model

Permissions follow the format: `{ACTION}_{RESOURCE}`

Examples:
- `READ_HOTSPOTS`
- `WRITE_HOTSPOTS`
- `DELETE_USERS`
- `MANAGE_ROLES`

## Security

### JWT Configuration
- **Access Token**: 24 hours expiration
- **Refresh Token**: 7 days expiration
- **Algorithm**: HS256 (HMAC with SHA-256)

### Password Security
- Passwords hashed using BCrypt
- Minimum 6 characters required
- Salt automatically generated

### Environment Variables (Production)
```bash
export JWT_SECRET=your-very-strong-secret-key-here
export DB_PASSWORD=your-database-password
```

## Integration with Other Services

### API Gateway Integration
The API Gateway should forward requests with JWT tokens:

```java
// In API Gateway
@FeignClient(name = "auth-service", url = "http://localhost:8082")
public interface AuthServiceClient {
    
    @PostMapping("/api/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);
}
```

### Core Service Integration
Core service can verify permissions:

```java
// Check permission in Core Service
@PreAuthorize("hasAuthority('READ_HOTSPOTS')")
@GetMapping("/api/cultural-hotspots")
public List<CulturalHotSpot> getAll() {
    // ...
}
```

## Testing

### Create Test User
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Use Token
```bash
curl -X GET http://localhost:8082/api/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Swagger/OpenAPI Documentation

Access interactive API documentation at:
```
http://localhost:8082/swagger-ui.html
```

## Database Migrations

For production, use a migration tool:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

## Monitoring

### Health Check
```http
GET /actuator/health
```

### Metrics
```http
GET /actuator/metrics
```

## Service Layer Documentation

### Service Classes Overview

#### 1. CustomUserDetailsService
**Package**: `com.toronto.opendata.auth.service`

**Purpose**: Integrates with Spring Security to load user authentication details.

**Key Methods**:
- `loadUserByUsername(String username)`: Loads user from database and converts to Spring Security's UserDetails

**Features**:
- Fetches user with eager-loaded roles and permissions
- Converts roles to Spring Security GrantedAuthority with `ROLE_` prefix
- Converts permissions to GrantedAuthority format
- Throws `UsernameNotFoundException` if user not found

**Usage**: Automatically invoked by Spring Security during authentication.

---

#### 2. AuthService
**Package**: `com.toronto.opendata.auth.service`

**Purpose**: Handles authentication operations (register, login, token refresh).

**Key Methods**:

**`register(RegisterRequest request)`**
- Creates new user account
- Validates username and email uniqueness
- Hashes password with BCrypt
- Assigns default USER role if no roles specified
- Returns JWT token response

**`login(LoginRequest request)`**
- Authenticates user credentials
- Generates access token (24hr) and refresh token (7 days)
- Updates user's last login timestamp
- Returns JWT token response with user details

**`refreshToken(String refreshToken)`**
- Validates refresh token
- Generates new access and refresh tokens
- Returns new JWT token response

**Dependencies**:
- `UserRepository`: Database access
- `RoleRepository`: Role assignment
- `PasswordEncoder`: BCrypt hashing
- `JwtTokenProvider`: Token generation
- `AuthenticationManager`: Spring Security authentication

---

#### 3. UserService
**Package**: `com.toronto.opendata.auth.service`

**Purpose**: Complete user management operations.

**Key Methods**:

**User Retrieval**:
- `getAllUsers()`: Fetch all users
- `getUserById(Long id)`: Get user by ID
- `getUserByUsername(String username)`: Find by username
- `getUserByEmail(String email)`: Find by email

**User Modification**:
- `updateUser(Long id, User userUpdate)`: Update user profile (firstName, lastName, email)
- `changePassword(Long id, String oldPassword, String newPassword)`: Change user password with validation
- `setUserEnabled(Long id, boolean enabled)`: Enable/disable user account
- `setUserLocked(Long id, boolean locked)`: Lock/unlock user account

**Role Management**:
- `assignRole(Long userId, Long roleId)`: Add role to user
- `removeRole(Long userId, Long roleId)`: Remove role from user

**User Deletion**:
- `deleteUser(Long id)`: Permanently delete user

**Features**:
- Email uniqueness validation on updates
- Old password verification for password changes
- BCrypt password encoding
- Transactional operations

---

#### 4. RoleService
**Package**: `com.toronto.opendata.auth.service`

**Purpose**: Role and role-permission management.

**Key Methods**:

**Role Retrieval**:
- `getAllRoles()`: Fetch all roles
- `getRoleById(Long id)`: Get role by ID
- `getRoleByName(String name)`: Find by name

**Role Modification**:
- `createRole(String name, String description)`: Create new role
- `updateRole(Long id, String name, String description)`: Update role details
- `deleteRole(Long id)`: Delete role (clears user and permission associations)

**Permission Management**:
- `assignPermission(Long roleId, Long permissionId)`: Add permission to role
- `removePermission(Long roleId, Long permissionId)`: Remove permission from role
- `getRolePermissions(Long roleId)`: Get all permissions for a role

**Features**:
- Role name uniqueness validation
- Automatic cleanup of associations on deletion
- Transactional operations

---

#### 5. PermissionService
**Package**: `com.toronto.opendata.auth.service`

**Purpose**: Permission management with resource-action model.

**Key Methods**:

**Permission Retrieval**:
- `getAllPermissions()`: Fetch all permissions
- `getPermissionById(Long id)`: Get permission by ID
- `getPermissionsByResource(String resource)`: Find by resource (e.g., "hotspots")
- `getPermissionsByAction(String action)`: Find by action (e.g., "READ")
- `getPermissionByResourceAndAction(String resource, String action)`: Find specific permission

**Permission Modification**:
- `createPermission(String resource, String action, String description)`: Create new permission
- `updatePermission(Long id, String resource, String action, String description)`: Update permission
- `deletePermission(Long id)`: Delete permission (clears role associations)

**Validation**:
- `permissionExists(String resource, String action)`: Check if permission exists

**Features**:
- Resource-action uniqueness validation
- Prevents duplicate permissions
- Automatic cleanup of role associations on deletion
- Transactional operations

---

### Service Layer Architecture

```
Controllers (Not yet implemented)
    ↓
Service Layer (Transaction boundary)
    ↓
Repository Layer (JPA)
    ↓
PostgreSQL Database
```

### Transaction Management
All service methods are annotated with `@Transactional`:
- Read-only methods: `@Transactional(readOnly = true)` for performance
- Write methods: `@Transactional` for ACID compliance
- Automatic rollback on exceptions

### Exception Handling
Current implementation throws `RuntimeException` for:
- Entity not found errors
- Validation failures (duplicate username/email/role/permission)
- Authentication failures

**Future Enhancement**: Create custom exception classes:
- `UserNotFoundException`
- `RoleNotFoundException`
- `PermissionNotFoundException`
- `DuplicateEntityException`
- `InvalidCredentialsException`

### Logging Strategy
All services use SLF4J with Lombok's `@Slf4j`:
- **DEBUG**: Query operations (find, fetch)
- **INFO**: Mutation operations (create, update, delete, login)
- **Log Format**: Includes entity IDs, usernames, operation details

### Security Considerations
- Passwords never logged
- BCrypt with auto-generated salts
- Old password verification required for password changes
- Email uniqueness enforced
- Role/Permission associations properly maintained

---

## Tech Stack
- Java 17
- Spring Boot 3.5.7
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (jjwt 0.12.3)
- Lombok
- SpringDoc OpenAPI

## Development Progress

### ✅ Completed
- Database entities (User, Role, Permission)
- JPA repositories with custom queries
- JWT token provider (generation & validation)
- Complete service layer (5 services)
- DTO classes (LoginRequest, RegisterRequest, JwtResponse, MessageResponse)
- Environment-specific configurations
- PostgreSQL integration

### 🔄 In Progress
- Spring Security configuration
- REST Controllers
- Data initialization service

### 📋 Next Steps
1. Implement Spring Security configuration (SecurityFilterChain, JWT filter)
2. Create REST Controllers (AuthController, UserController, RoleController, PermissionController)
3. Add data initialization service (default roles, admin user)
4. Add email verification
5. Implement password reset
6. Add OAuth2 support (Google, GitHub)
7. Add rate limiting
8. Implement audit logging
9. Add 2FA support

## Support
For issues or questions, create an issue in the GitHub repository.

# Authentication System Documentation

## Overview

This document describes the JWT-based authentication system for the Housemate platform.

## Components

### 1. DTOs (Data Transfer Objects)

#### RegisterRequest
```java
{
  "firstName": "string (2-100 chars)",
  "lastName": "string (2-100 chars)",
  "email": "valid email (max 255 chars)",
  "password": "string (8-100 chars)"
}
```

#### LoginRequest
```java
{
  "email": "valid email",
  "password": "string"
}
```

#### AuthResponse
```java
{
  "token": "JWT token string",
  "userId": "UUID",
  "email": "string",
  "role": "USER | ADMIN",
  "firstName": "string",
  "lastName": "string"
}
```

### 2. API Endpoints

#### Register a New User
```
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}

Response: 201 Created
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "john.doe@example.com",
  "role": "USER",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "john.doe@example.com",
  "role": "USER",
  "firstName": "John",
  "lastName": "Doe"
}
```

### 3. Using JWT Token

Include the JWT token in the Authorization header for protected endpoints:

```
GET /api/v1/listings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Security Configuration

#### Public Endpoints (No Authentication Required)
- `/api/v1/auth/**` - All authentication endpoints
- `/v3/api-docs/**` - OpenAPI documentation
- `/swagger-ui/**` - Swagger UI
- `/swagger-ui.html` - Swagger UI HTML
- `/error` - Error handling

#### Protected Endpoints
All other endpoints require a valid JWT token in the Authorization header.

### 5. JWT Configuration

JWT settings can be configured in `application.yaml`:

```yaml
jwt:
  secret-key: ${JWT_SECRET_KEY:your-secret-key-here}
  expiration: 86400000 # 24 hours in milliseconds
```

**Production Note:** Always set `JWT_SECRET_KEY` as an environment variable in production. The default key is for development only.

### 6. Password Security

- Passwords are hashed using BCrypt before storage
- Minimum password length: 8 characters
- Maximum password length: 100 characters

### 7. Error Responses

#### 400 Bad Request (Validation Error)
```json
{
  "timestamp": "2026-03-06T20:22:56.789Z",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must be between 8 and 100 characters"
  }
}
```

#### 400 Bad Request (Email Already Exists)
```json
{
  "timestamp": "2026-03-06T20:22:56.789Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already registered: john.doe@example.com"
}
```

#### 401 Unauthorized (Invalid Credentials)
```json
{
  "timestamp": "2026-03-06T20:22:56.789Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

### 8. Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

View the OpenAPI specification at:
```
http://localhost:8080/v3/api-docs
```

## Testing with cURL

### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123"
  }'
```

### Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/v1/listings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Architecture Details

### Component Breakdown

1. **JwtService**: Handles JWT token generation, validation, and claims extraction
2. **JwtAuthenticationFilter**: Intercepts requests to validate JWT tokens
3. **UserDetailsServiceImpl**: Loads user details from the database for Spring Security
4. **UserDetailsImpl**: Wrapper around User entity implementing UserDetails
5. **SecurityConfig**: Configures Spring Security with JWT authentication
6. **AuthService**: Business logic for registration and login
7. **AuthController**: REST endpoints for authentication
8. **GlobalExceptionHandler**: Centralized exception handling

### Security Features

- ✅ Stateless authentication (no server-side sessions)
- ✅ JWT-based token authentication
- ✅ BCrypt password hashing
- ✅ Role-based access control (USER, ADMIN)
- ✅ Email as unique username
- ✅ Soft delete support
- ✅ Account status validation (ACTIVE/DELETED)
- ✅ CSRF protection disabled (stateless API)

## Next Steps

1. Implement email verification
2. Add password reset functionality
3. Implement refresh tokens
4. Add rate limiting
5. Implement OAuth2 social login
6. Add two-factor authentication (2FA)

# Attribute Authority Service

A microservice for managing attribute-based encryption keys, hospital registration, and role-based access control in the ABE (Attribute-Based Encryption) Healthcare system.

## Overview

The Attribute Authority Service is the central authority for managing encryption keys, hospital registrations, and attribute-based access control. It generates and distributes encryption keys for roles and specializations, enabling fine-grained access control to encrypted healthcare data.

## Features

- **Key Management**: Generate and manage encryption keys for roles and specializations
- **Hospital Management**: Register and manage hospital entities
- **Authentication**: JWT-based authentication for Attribute Authority administrators
- **Key Distribution**: Secure key distribution for encryption operations
- **Role-Based Keys**: Generate keys for different roles (Doctor, Nurse, etc.)
- **Specialization Keys**: Generate keys for medical specializations

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Service Discovery**: Netflix Eureka Client
- **Inter-Service Communication**: OpenFeign
- **Encryption**: AES-GCM, Elliptic Curve Cryptography

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database
- Eureka Server (for service discovery)

## Configuration

### Application Properties

Update `src/main/resources/application.properties`:

```properties
spring.application.name=AttributeAuthority
server.port=8080

# Database Configuration
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/Attribute_Authority
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# AES Encryption Key
aes.key=your_base64_encoded_key
```

## Installation

1. **Clone the repository**:
   ```bash
   cd AttributeAuthority
   ```

2. **Configure database**:
   - Create PostgreSQL database: `Attribute_Authority`
   - Update database credentials in `application.properties`

3. **Build the project**:
   ```bash
   mvn clean install
   ```

4. **Run the service**:
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR:
   ```bash
   java -jar target/AttributeAuthority-0.0.1-SNAPSHOT.jar
   ```

## API Endpoints

### Authentication

#### Login
- **Endpoint**: `POST /AA/login`
- **Description**: Authenticate Attribute Authority administrator
- **Request Body**:
  ```json
  {
    "adharNo": "admin_aadhar",
    "password": "admin_password"
  }
  ```
- **Response**:
  ```json
  {
    "message": "jwt_token",
    "logtime": "2024-01-01T12:00:00"
  }
  ```

#### Logout
- **Endpoint**: `POST /AA/logout`
- **Description**: Logout and invalidate JWT token
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
  ```json
  {
    "message": "Logout successfully"
  }
  ```

#### Reset Password
- **Endpoint**: `POST /AA/resetPass`
- **Description**: Reset administrator password
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
  ```json
  {
    "adharNo": "",
    "phNo": "",
    "password": "new_password"
  }
  ```

### Key Management

#### Generate Key
- **Endpoint**: `POST /AA/genKey?role={role}`
- **Description**: Generate encryption key pair for a role or specialization
- **Headers**: `Authorization: Bearer {token}` (optional, some endpoints are public)
- **Query Parameters**:
  - `role`: Role or specialization name (e.g., "Doctor", "Nurse", "Cardiology")
- **Response**:
  ```json
  {
    "message": "Key generated successfully",
    "publicKey": "base64_public_key"
  }
  ```

#### Get Key
- **Endpoint**: `POST /AA/getKey?role={role}`
- **Description**: Retrieve public key for a role or specialization
- **Query Parameters**:
  - `role`: Role or specialization name
- **Response**:
  ```json
  {
    "message": "base64_public_key"
  }
  ```

#### Give Secret Key
- **Endpoint**: `POST /AA/giveSecretKey?pubKey={public_key}&role1={role1}&role2={role2}`
- **Description**: Generate encrypted role and specialization keys for a patient's public key
- **Query Parameters**:
  - `pubKey`: Patient's public key (Base64)
  - `role1`: Primary role (e.g., "Doctor")
  - `role2`: Specialization or "N/A"
- **Response**:
  ```json
  {
    "role": "encrypted_role_key",
    "spec": "encrypted_specialization_key"
  }
  ```

#### Set Public Key
- **Endpoint**: `POST /AA/setPublicKey?key={public_key}`
- **Description**: Store Attribute Authority administrator's public key
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `key`: Base64-encoded public key

### Hospital Management

#### Create Hospital
- **Endpoint**: `POST /AA/createHospital`
- **Description**: Register a new hospital
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
  ```json
  {
    "id": "HOSP001",
    "name": "General Hospital",
    "pass": "hospital_password"
  }
  ```
- **Response**:
  ```json
  {
    "message": "Hospital created successfully"
  }
  ```

#### See All Hospitals
- **Endpoint**: `POST /AA/seeAllHospital`
- **Description**: List all registered hospitals
- **Headers**: `Authorization: Bearer {token}`
- **Response**: List of `Hospitals`

#### Revoke Hospital
- **Endpoint**: `POST /AA/revokeHospital?id={hospital_id}`
- **Description**: Revoke hospital access
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `id`: Hospital ID
- **Response**:
  ```json
  {
    "message": "Hospital revoked successfully"
  }
  ```

## Data Models

### UserData
- `adharNo`: Administrator Aadhar number (Primary Key)
- `phNo`: Phone number
- `password`: BCrypt hashed password
- `publicKey`: Base64-encoded public key
- `lastLoginTime`: Last login timestamp

### Hospitals
- `id`: Hospital ID (Primary Key)
- `name`: Hospital name
- `pass`: BCrypt hashed password
- `publicKey`: Base64-encoded public key
- `lastLoginTime`: Last login timestamp

### Key
- `role`: Role or specialization name (Primary Key)
- `publicKey`: Base64-encoded public key
- `privateKey`: Encrypted private key (AES-GCM)

### Staff
- `regNo`: Staff registration number
- `name`: Staff name
- `designation`: Role designation
- `specialization`: Medical specialization

## Security

### Authentication Flow
1. Administrator provides Aadhar number and password
2. Service validates credentials using Spring Security
3. JWT token generated and returned
4. Token used for subsequent authenticated requests

### Key Generation
- Elliptic Curve (EC) key pairs using secp256r1 curve
- Public keys stored in database
- Private keys encrypted using AES-GCM before storage
- Keys generated for roles and specializations

### Key Distribution
- Public keys distributed to authorized services
- Private keys encrypted and stored securely
- Role and specialization keys encrypted with patient public keys for access control

### Public Endpoints
Some endpoints are publicly accessible (no authentication required):
- `/AA/login`
- `/AA/logout`
- `/AA/getKey`
- `/AA/giveSecretKey`

## Inter-Service Communication

### Feign Clients

#### UserFeign (UserService)
- Used for inter-service communication (if needed)

## Database Schema

The service uses the following tables:
- `user_data`: Administrator accounts
- `hospitals`: Hospital registrations
- `key`: Encryption key storage
- `staff`: Staff member information (if applicable)

## Error Handling

Common error responses:

- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Invalid credentials or expired token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found (e.g., role key not found)
- **500 Internal Server Error**: Server-side error

## Key Generation Process

1. **Role Key Generation**:
   - Generate EC key pair for role (e.g., "Doctor")
   - Store public key in database
   - Encrypt private key with AES-GCM
   - Store encrypted private key

2. **Key Distribution**:
   - Patient requests role public key
   - Service returns public key
   - Patient uses public key for encryption

3. **Secret Key Generation**:
   - Patient provides their public key
   - Service encrypts role/specialization private keys with patient's public key
   - Returns encrypted keys for patient to store

## Logging

Enable debug logging:
```properties
logging.level.feign=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Testing

Run tests:
```bash
mvn test
```

## Dependencies

Key dependencies:
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- Spring Cloud OpenFeign
- Spring Cloud Netflix Eureka Client
- PostgreSQL Driver
- JWT (jjwt)
- Lombok

## Port

Default port: **8080**

## Health Check

The service registers with Eureka Server for service discovery and health monitoring.

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. **Key Generation Fails**
   - Check AES key configuration
   - Verify encryption service initialization
   - Check database connection

3. **Eureka Connection Failed**
   - Verify Eureka Server is running
   - Check Eureka server URL

4. **JWT Token Invalid**
   - Check token expiration
   - Verify token format
   - Ensure token is included in Authorization header

5. **Key Not Found**
   - Ensure key is generated before retrieval
   - Check role/specialization name spelling
   - Verify key exists in database

## Development

### Project Structure
```
AttributeAuthority/
├── src/main/java/com/Suchorit/AttributeAuthority/
│   ├── config/          # Security, JWT, and crypto configuration
│   ├── controller/      # REST controllers and Feign clients
│   ├── model/           # Entity models
│   ├── repo/            # JPA repositories
│   └── service/         # Business logic and key generation
└── src/main/resources/
    └── application.properties
```


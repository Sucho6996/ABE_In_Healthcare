# Doctor Service

A microservice for managing doctor/staff authentication, authorization, and access to patient prescription records in the ABE (Attribute-Based Encryption) Healthcare system.

## Overview

The Doctor Service handles authentication and authorization for medical staff (doctors, nurses, management) and provides secure access to encrypted patient prescription data based on role and specialization attributes.

## Features

- **Staff Authentication**: JWT-based login/logout for medical staff
- **Password Management**: Secure password reset functionality
- **Prescription Access**: Role and specialization-based access to patient records
- **Key Management**: Public key storage and retrieval for encryption operations
- **Hospital Records**: Access to all prescriptions from the staff member's hospital
- **Professional Records**: Access to prescriptions based on role and specialization

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Service Discovery**: Netflix Eureka Client
- **Inter-Service Communication**: OpenFeign
- **Encryption**: AES-GCM, ECDH key exchange

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database
- Eureka Server (for service discovery)
- UserService (for prescription data)
- AttributeAuthority Service (for key management)

## Configuration

### Application Properties

Update `src/main/resources/application.properties`:

```properties
spring.application.name=Doctor
server.port=8082

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
   cd Doctor
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
   java -jar target/Doctor-0.0.1-SNAPSHOT.jar
   ```

## API Endpoints

### Authentication

#### Login
- **Endpoint**: `POST /staff/login`
- **Description**: Authenticate staff member
- **Request Body**:
  ```json
  {
    "regNo": "DOC123456",
    "pass": "password"
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
- **Endpoint**: `POST /staff/logout`
- **Description**: Logout and invalidate JWT token
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
  ```json
  {
    "message": "Logout successfully"
  }
  ```

#### Reset Password
- **Endpoint**: `POST /staff/resetPass`
- **Description**: Reset staff password
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
  ```json
  {
    "regNo": "DOC123456",
    "pass": "new_password"
  }
  ```

### Key Management

#### Set Public Key
- **Endpoint**: `POST /staff/setPublicKey?key={public_key}`
- **Description**: Store staff member's public key
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `key`: Base64-encoded public key

#### Get Key
- **Endpoint**: `POST /staff/getKey`
- **Description**: Retrieve encryption keys for authenticated staff
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
  ```json
  {
    "pubKey": "base64_public_key",
    "role": "encrypted_role_key",
    "spec": "encrypted_specialization_key"
  }
  ```

### Prescription Access

#### Get Prescriptions by Hospital
- **Endpoint**: `POST /staff/getPDetailsByHos`
- **Description**: Get all prescriptions from staff member's hospital
- **Headers**: `Authorization: Bearer {token}`
- **Response**: List of `PatientDetails`

#### Get Prescriptions by Profession
- **Endpoint**: `POST /staff/getPDetailsByProf`
- **Description**: Get prescriptions accessible based on role and specialization
- **Headers**: `Authorization: Bearer {token}`
- **Response**: List of `PatientDetails`

#### Get Prescription Details
- **Endpoint**: `POST /staff/getPrescription?id={prescription_id}`
- **Description**: Get specific prescription with encrypted image
- **Query Parameters**:
  - `id`: Prescription ID
- **Response**:
  ```json
  {
    "image": "base64_encrypted_image",
    "key": "patient_public_key",
    "spec": "specialization_or_N/A"
  }
  ```

### Utility

#### Check Last Login
- **Endpoint**: `POST /staff/cll`
- **Description**: Check and update last login time
- **Headers**: `Authorization: Bearer {token}`

## Data Models

### Staff
- `regNo`: Registration number (Primary Key)
- `name`: Staff name
- `designation`: Role (Doctor/Nurse/Management)
- `specialization`: Medical specialization
- `password`: BCrypt hashed password
- `publicKey`: Base64-encoded public key
- `lastLoginTime`: Last login timestamp
- `hospitalId`: Associated hospital ID

### PatientDetails
- `id`: Prescription ID
- `adharNo`: Patient Aadhar number
- `hosId`: Hospital ID
- `allowedRole`: Required role for access
- `allowedSpecialization`: Required specialization
- `image`: Encrypted prescription image
- `uploadDate`: Upload timestamp

## Security

### Authentication Flow
1. Staff provides registration number and password
2. Service validates credentials using Spring Security
3. JWT token generated and returned
4. Token used for subsequent authenticated requests

### Authorization
- Role-based access control (Doctor, Nurse, Management)
- Specialization-based access for specialized prescriptions
- Hospital-based access for hospital records

### Encryption
- Public keys stored for ECDH key exchange
- Role and specialization keys encrypted using AES-GCM
- Prescription images encrypted with layered encryption

## Inter-Service Communication

### Feign Clients

#### UserFeign (UserService)
- `getPDetailsByHos(hosId)`: Get prescriptions by hospital
- `getPDetailsByProf(role, spec)`: Get prescriptions by profession
- `getPrescription(id)`: Get prescription details

#### AAFeign (AttributeAuthority)
- `retrieve(role)`: Get role public key
- `giveSecretKey(pubKey, role1, role2)`: Get encrypted role/specialization keys

## Database Schema

The service uses the following tables:
- `staff`: Staff member information
- `key`: Encryption key storage

## Error Handling

Common error responses:

- **401 Unauthorized**: Invalid credentials or expired token
- **400 Bad Request**: Invalid request parameters
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

## Logging

Enable debug logging in `application.properties`:
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

Default port: **8082**

## Health Check

The service registers with Eureka Server for service discovery and health monitoring.

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. **Eureka Connection Failed**
   - Verify Eureka Server is running
   - Check Eureka server URL

3. **JWT Token Invalid**
   - Check token expiration
   - Verify token format
   - Ensure token is included in Authorization header

4. **Feign Client Errors**
   - Verify UserService and AttributeAuthority are running
   - Check service names in Eureka
   - Verify network connectivity

## Development

### Project Structure
```
Doctor/
├── src/main/java/com/Suchorit/Doctor/
│   ├── config/          # Security and JWT configuration
│   ├── controller/      # REST controllers and Feign clients
│   ├── model/           # Entity models
│   ├── repo/            # JPA repositories
│   └── service/         # Business logic
└── src/main/resources/
    └── application.properties
```



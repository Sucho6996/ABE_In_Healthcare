# Hospital Service

A microservice for managing hospital authentication, staff registration, and staff management in the ABE (Attribute-Based Encryption) Healthcare system.

## Overview

The Hospital Service handles hospital authentication and provides functionality for hospitals to manage their medical staff (doctors, nurses, management). It enables hospitals to register staff members with appropriate roles and specializations for the attribute-based encryption system.

## Features

- **Hospital Authentication**: JWT-based login/logout for hospitals
- **Staff Management**: Add, view, and remove medical staff
- **Password Management**: Secure password reset functionality
- **Key Management**: Public key storage for encryption operations
- **Role Assignment**: Assign roles and specializations to staff members

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Service Discovery**: Netflix Eureka Client
- **Inter-Service Communication**: OpenFeign

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database
- Eureka Server (for service discovery)
- AttributeAuthority Service (for hospital registration)

## Configuration

### Application Properties

Update `src/main/resources/application.properties`:

```properties
spring.application.name=Hospital
server.port=8081

# Database Configuration
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/Attribute_Authority
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Installation

1. **Clone the repository**:
   ```bash
   cd Hospital
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
   java -jar target/Hospital-0.0.1-SNAPSHOT.jar
   ```

## API Endpoints

### Authentication

#### Login
- **Endpoint**: `POST /hospital/login`
- **Description**: Authenticate hospital
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
    "message": "jwt_token",
    "logtime": "2024-01-01T12:00:00"
  }
  ```

#### Logout
- **Endpoint**: `POST /hospital/logout`
- **Description**: Logout and invalidate JWT token
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
  ```json
  {
    "message": "Logout successfully"
  }
  ```

#### Reset Password
- **Endpoint**: `POST /hospital/resetPass`
- **Description**: Reset hospital password
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
  ```json
  {
    "id": "HOSP001",
    "name": "General Hospital",
    "pass": "new_password"
  }
  ```

### Staff Management

#### Add Staff
- **Endpoint**: `POST /hospital/addStaff`
- **Description**: Register a new staff member (doctor, nurse, or management)
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
  ```json
  {
    "regNo": "DOC123456",
    "name": "Dr. John Doe",
    "designation": "Doctor",
    "specialization": "Cardiology",
    "pass": "staff_password",
    "hospitalId": "HOSP001"
  }
  ```
- **Response**:
  ```json
  {
    "message": "Staff added successfully"
  }
  ```

#### See All Staff
- **Endpoint**: `POST /hospital/seeAllStaff`
- **Description**: List all staff members of the authenticated hospital
- **Headers**: `Authorization: Bearer {token}`
- **Response**: List of `Staff` objects
  ```json
  [
    {
      "regNo": "DOC123456",
      "name": "Dr. John Doe",
      "designation": "Doctor",
      "specialization": "Cardiology",
      "hospitalId": "HOSP001"
    }
  ]
  ```

#### Remove Staff
- **Endpoint**: `POST /hospital/removeStaff?regNo={registration_number}`
- **Description**: Remove/revoke a staff member's access
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `regNo`: Staff registration number
- **Response**:
  ```json
  {
    "message": "Staff has been revoked"
  }
  ```

### Key Management

#### Set Public Key
- **Endpoint**: `POST /hospital/setPublicKey?key={public_key}`
- **Description**: Store hospital's public key
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `key`: Base64-encoded public key

## Data Models

### Hospitals
- `id`: Hospital ID (Primary Key)
- `name`: Hospital name
- `pass`: BCrypt hashed password
- `publicKey`: Base64-encoded public key
- `lastLoginTime`: Last login timestamp

### Staff
- `regNo`: Registration number (Primary Key)
- `name`: Staff member name
- `designation`: Role (Doctor, Nurse, Management)
- `specialization`: Medical specialization (e.g., "Cardiology", "N/A")
- `password`: BCrypt hashed password
- `publicKey`: Base64-encoded public key
- `hospitalId`: Associated hospital ID
- `lastLoginTime`: Last login timestamp

## Security

### Authentication Flow
1. Hospital provides ID and password
2. Service validates credentials using Spring Security
3. JWT token generated and returned
4. Token used for subsequent authenticated requests

### Authorization
- Hospitals can only manage their own staff
- Staff operations require valid hospital authentication
- Staff registration validated against hospital ID

### Password Security
- Passwords hashed using BCrypt with strength 12
- Passwords never stored in plain text

## Staff Registration Process

1. **Hospital Login**: Hospital authenticates and receives JWT token
2. **Add Staff**: Hospital provides staff details:
   - Registration number (unique identifier)
   - Name
   - Designation (Doctor/Nurse/Management)
   - Specialization (medical specialization or "N/A")
   - Password
3. **Staff Created**: Staff member registered in system
4. **Key Generation**: Staff generates EC key pair (client-side)
5. **Public Key Storage**: Staff's public key stored via Doctor Service

## Designation Types

- **Doctor**: Medical doctors with potential specializations
- **Nurse**: Nursing staff
- **Management**: Hospital administrative staff

## Specialization

Specializations can include:
- Cardiology
- Neurology
- Orthopedics
- Pediatrics
- General Medicine
- "N/A" (no specialization required)

## Database Schema

The service uses the following tables:
- `hospitals`: Hospital information
- `staff`: Staff member information

## Error Handling

Common error responses:

- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Invalid credentials or expired token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found (e.g., staff member not found)
- **500 Internal Server Error**: Server-side error

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

Default port: **8081**

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

4. **Staff Registration Fails**
   - Verify hospital is authenticated
   - Check staff registration number uniqueness
   - Ensure hospital ID matches authenticated hospital

5. **Staff Not Found**
   - Verify staff registration number
   - Check staff belongs to authenticated hospital
   - Ensure staff exists in database

## Integration with Other Services

### Doctor Service
- Staff registered in Hospital Service can authenticate via Doctor Service
- Staff public keys stored in Doctor Service
- Staff can access prescriptions based on role and specialization

### Attribute Authority Service
- Hospitals must be registered in Attribute Authority Service
- Hospital registration managed by Attribute Authority administrators

## Development

### Project Structure
```
Hospital/
├── src/main/java/com/Suchorit/Hospital/
│   ├── config/          # Security and JWT configuration
│   ├── controller/      # REST controllers
│   ├── model/           # Entity models
│   ├── repo/            # JPA repositories
│   └── service/         # Business logic
└── src/main/resources/
    └── application.properties
```


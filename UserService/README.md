# User Service

A microservice for managing patient accounts, authentication, and encrypted prescription management in the ABE (Attribute-Based Encryption) Healthcare system.

## Overview

The User Service handles patient registration, authentication, and secure prescription upload/retrieval. It implements attribute-based encryption to ensure only authorized medical staff can access patient prescriptions based on role and specialization.

## Features

- **Patient Registration**: Secure account creation with Aadhar-based identification
- **Authentication**: JWT-based login/logout for patients
- **Prescription Management**: Upload and retrieve encrypted prescriptions
- **Attribute-Based Access**: Role and specialization-based prescription access control
- **Key Management**: Public key storage and retrieval for encryption operations
- **Account Management**: Account deactivation functionality

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Service Discovery**: Netflix Eureka Client
- **Inter-Service Communication**: OpenFeign
- **File Upload**: Multipart file handling

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database
- Eureka Server (for service discovery)
- AttributeAuthority Service (for key management)

## Configuration

### Application Properties

Update `src/main/resources/application.properties`:

```properties
spring.application.name=UserService
server.port=8090

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
   cd UserService
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
   java -jar target/UserService-0.0.1-SNAPSHOT.jar
   ```

## API Endpoints

### Authentication

#### Signup
- **Endpoint**: `POST /user/signup`
- **Description**: Register a new patient account
- **Request Body**:
  ```json
  {
    "adharNo": "123456789012",
    "phNo": "9876543210",
    "password": "secure_password"
  }
  ```
- **Response**:
  ```json
  {
    "message": "Succesfully Account created"
  }
  ```

#### Login
- **Endpoint**: `POST /user/login`
- **Description**: Authenticate patient
- **Request Body**:
  ```json
  {
    "adharNo": "123456789012",
    "phNo": "",
    "password": "secure_password"
  }
  ```
- **Response**:
  ```json
  {
    "message": "jwt_token"
  }
  ```

#### Logout
- **Endpoint**: `POST /user/logout`
- **Description**: Logout and invalidate JWT token
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
  ```json
  {
    "message": "Logout successfully"
  }
  ```

#### Deactivate Account
- **Endpoint**: `POST /user/deactivate`
- **Description**: Permanently delete patient account
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
  ```json
  {
    "message": "Deleted successfully"
  }
  ```

### Key Management

#### Set Public Key
- **Endpoint**: `POST /user/setPublicKey?adharNo={aadhar}&key={public_key}`
- **Description**: Store patient's public key
- **Query Parameters**:
  - `adharNo`: Patient Aadhar number
  - `key`: Base64-encoded public key

#### Get Public Key
- **Endpoint**: `POST /user/getPubKey?adharNo={aadhar}`
- **Description**: Retrieve patient's public key
- **Query Parameters**:
  - `adharNo`: Patient Aadhar number

#### Get Key (Role)
- **Endpoint**: `POST /user/getKey?role={role}`
- **Description**: Get public key for a specific role (Doctor/Nurse)
- **Query Parameters**:
  - `role`: Role name (e.g., "Doctor", "Nurse")

### Prescription Management

#### Upload Prescription
- **Endpoint**: `POST /user/upload`
- **Description**: Upload encrypted prescription image
- **Headers**: `Authorization: Bearer {token}`
- **Content-Type**: `multipart/form-data`
- **Request Parts**:
  - `patientDetails`: JSON object
    ```json
    {
      "hosId": "HOSP001",
      "allowedRole": "Doctor",
      "allowedSpecialization": "Cardiology"
    }
    ```
  - `img`: Encrypted image file (MultipartFile)
- **Response**:
  ```json
  {
    "message": "Prescription uploaded successfully"
  }
  ```

#### Get All Details
- **Endpoint**: `POST /user/getAllDetails`
- **Description**: Get all prescriptions for authenticated patient
- **Headers**: `Authorization: Bearer {token}`
- **Response**: List of `PatientDetails`

#### Get Details
- **Endpoint**: `POST /user/getDetails?id={prescription_id}`
- **Description**: Get specific prescription with encrypted image
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `id`: Prescription ID
- **Response**:
  ```json
  {
    "image": "base64_encrypted_image",
    "key": "patient_public_key"
  }
  ```

#### Get Secret Key
- **Endpoint**: `POST /user/getSecretKey?id={prescription_id}`
- **Description**: Get encryption keys for prescription decryption
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `id`: Prescription ID
- **Response**:
  ```json
  {
    "pubKey": "base64_public_key",
    "role": "encrypted_role_key",
    "spec": "encrypted_specialization_key"
  }
  ```

### Prescription Access (For Other Services)

#### Get Prescriptions by Hospital
- **Endpoint**: `POST /user/getPDetailsByHos?hosId={hospital_id}`
- **Description**: Get all prescriptions from a specific hospital
- **Query Parameters**:
  - `hosId`: Hospital ID
- **Response**: List of `PatientDetails`

#### Get Prescriptions by Profession
- **Endpoint**: `POST /user/getPDetailsByProf?role={role}&spec={specialization}`
- **Description**: Get prescriptions accessible by role and specialization
- **Query Parameters**:
  - `role`: Role (Doctor/Nurse)
  - `spec`: Specialization or "N/A"
- **Response**: List of `PatientDetails`

#### Get Prescription
- **Endpoint**: `POST /user/getPrescription?id={prescription_id}`
- **Description**: Get prescription details (used by Doctor Service)
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

#### Give Key
- **Endpoint**: `POST /user/giveKey?id={prescription_id}`
- **Description**: Provide encryption key for prescription
- **Query Parameters**:
  - `id`: Prescription ID

## Data Models

### UserData
- `adharNo`: Aadhar number (Primary Key)
- `phNo`: Phone number
- `password`: BCrypt hashed password
- `publicKey`: Base64-encoded public key

### PatientDetails
- `id`: Prescription ID (Primary Key, Auto-generated)
- `adharNo`: Patient Aadhar number
- `hosId`: Hospital ID
- `allowedRole`: Required role for access (Doctor/Nurse)
- `allowedSpecialization`: Required specialization or "N/A"
- `image`: Encrypted prescription image (Base64)
- `uploadDate`: Upload timestamp

## Security

### Authentication Flow
1. Patient provides Aadhar number and password
2. Service validates credentials using Spring Security
3. JWT token generated and returned
4. Token used for subsequent authenticated requests

### Encryption Scheme
1. **Patient Upload**:
   - Patient encrypts prescription with role public key
   - Optional specialization layer encryption
   - Final encryption with patient's public key
   - Encrypted image stored in database

2. **Access Control**:
   - Only authorized roles can access prescriptions
   - Specialization-based access for specialized prescriptions
   - Keys retrieved from AttributeAuthority service

### Password Security
- Passwords hashed using BCrypt with strength 12
- Passwords never stored in plain text

## Inter-Service Communication

### Feign Clients

#### AAFeign (AttributeAuthority)
- `retrieve(role)`: Get role public key
- `giveKey(patientDetails)`: Provide encryption keys
- `giveSecretKey(pubKey, role1, role2)`: Get encrypted role/specialization keys

## Database Schema

The service uses the following tables:
- `user_data`: Patient account information
- `patient_details`: Prescription records

## Error Handling

Common error responses:

- **400 Bad Request**: Invalid request parameters or account already exists
- **401 Unauthorized**: Invalid credentials or expired token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

## File Upload

- Maximum file size: Configured in Spring Boot properties
- Supported formats: Image files (PNG, JPEG)
- Files stored as Base64-encoded strings in database
- Encryption performed client-side before upload

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

Default port: **8090**

## Health Check

The service registers with Eureka Server for service discovery and health monitoring.

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. **File Upload Fails**
   - Check file size limits
   - Verify multipart configuration
   - Ensure image is properly encrypted

3. **Eureka Connection Failed**
   - Verify Eureka Server is running
   - Check Eureka server URL

4. **JWT Token Invalid**
   - Check token expiration
   - Verify token format
   - Ensure token is included in Authorization header

5. **Feign Client Errors**
   - Verify AttributeAuthority service is running
   - Check service names in Eureka
   - Verify network connectivity

## Development

### Project Structure
```
UserService/
├── src/main/java/com/Suchorit/UserService/
│   ├── config/          # Security and JWT configuration
│   ├── controller/      # REST controllers and Feign clients
│   ├── model/           # Entity models
│   ├── repo/            # JPA repositories
│   └── service/         # Business logic
└── src/main/resources/
    └── application.properties
```


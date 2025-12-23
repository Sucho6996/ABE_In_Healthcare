# ABE in Healthcare - Command Line Interface (CLI)

A command-line interface application for interacting with the Attribute-Based Encryption (ABE) Healthcare system. This CLI provides secure access to healthcare services for different user roles including Patients, Doctors, Hospitals, and Attribute Authorities.

## Overview

This CLI application enables secure communication with the ABE Healthcare microservices architecture. It implements cryptographic operations for secure prescription management, user authentication, and role-based access control using Elliptic Curve Cryptography (ECC) and AES-GCM encryption.

## Features

- **Multi-Role Support**: Access the system as User (Patient), Doctor, Hospital, or Attribute Authority
- **Secure Authentication**: JWT-based authentication with password reset functionality
- **Encrypted Prescription Management**: Upload, view, and manage encrypted medical prescriptions
- **Key Management**: Automatic generation and storage of cryptographic keys
- **Attribute-Based Access Control**: Role and specialization-based access to encrypted data
- **End-to-End Encryption**: Uses ECDH key exchange and AES-GCM encryption

## Architecture

### Components

1. **Main CLI (`Cli.java`)**: Entry point that provides role selection menu
2. **User CLI (`UserCli.java`)**: Patient interface for prescription management
3. **Doctor CLI (`DoctorCli.java`)**: Doctor interface for accessing patient records
4. **Hospital CLI (`HospitalCli.java`)**: Hospital administration interface
5. **Attribute Authority CLI (`AtributeAuth.java`)**: Attribute Authority management interface

### Utility Classes

- **`KeyGeneration.java`**: Generates EC key pairs and manages key storage
- **`EcKeyUtil.java`**: Utilities for loading and deriving ECDH keys
- **`AESGCM.java`**: AES-GCM encryption/decryption implementation
- **`EncryptionService.java`**: Encryption service wrapper

## Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher
- **JSON Library**: `json.jar` (included in `lib/` directory)
- **Backend Services**: All microservices must be running:
  - User Service (port 8090)
  - Hospital Service (port 8081)
  - Doctor Service (port 8082)
  - Attribute Authority Service (port 8080)
  - Server Service

## Installation

1. **Clone or navigate to the CLI directory**:
   ```bash
   cd Cli
   ```

2. **Ensure `json.jar` is in the `lib/` directory**

3. **Compile the Java files**:
   ```bash
   javac -cp "lib/json.jar" *.java
   ```

   Or compile individually:
   ```bash
   javac -cp "lib/json.jar" Cli.java UserCli.java DoctorCli.java HospitalCli.java AtributeAuth.java KeyGeneration.java EcKeyUtil.java AESGCM.java EncryptionService.java
   ```

## Usage

### Starting the CLI

Run the main CLI application:
```bash
java -cp ".:lib/json.jar" Cli
```

Or on Windows:
```bash
java -cp ".;lib/json.jar" Cli
```

### Main Menu

Upon starting, you'll see:
```
=== ABE Service CLI ===
You want to login as:
1. User
2. Doctor
3. Hospital
4. Attribute Authority
5. Exit
Your choice:
```

### User (Patient) Interface

**Features:**
- Signup: Create a new patient account
- Login: Authenticate with Aadhar number and password
- Upload Prescription: Encrypt and upload medical prescriptions
- View Prescriptions: Decrypt and view your prescriptions
- Deactivate Account: Delete your account

**Key Operations:**
1. **Signup**: Creates account, generates EC key pair, stores private key locally
2. **Upload Prescription**: 
   - Encrypts prescription image using ECDH key exchange
   - Supports role-based (Doctor/Nurse) and specialization-based access
   - Uploads encrypted file to server
3. **View Prescription**: 
   - Retrieves encrypted prescription
   - Decrypts using stored private key
   - Opens image in default viewer

**Private Key Storage**: Private keys are stored as `{aadharNumber}.txt` files in the CLI directory.

### Doctor Interface

**Features:**
- Login: Authenticate with registration number
- View Hospital Records: Access all prescriptions from your hospital
- View Accessible Records: View prescriptions you have access to based on role/specialization
- Reset Password: Change password and regenerate keys

**Key Operations:**
1. **Login**: Authenticates with registration number and password
2. **View Prescriptions**: 
   - Lists available prescriptions
   - Decrypts using role and specialization keys
   - Opens decrypted prescription images

**Private Key Storage**: Private keys are stored as `{registrationNumber}.txt` files.

### Hospital Interface

**Features:**
- Login: Authenticate with hospital ID
- Add Staff: Register new doctors/nurses
- View All Staff: List all hospital staff
- Remove Staff: Revoke staff access
- Reset Password: Change password

**Key Operations:**
1. **Add Staff**: 
   - Registers new staff member
   - Requires: Registration number, name, designation, specialization, password
2. **Manage Staff**: View and remove staff members

### Attribute Authority Interface

**Features:**
- Login: Authenticate as Attribute Authority
- Add Hospital: Register new hospitals
- View All Hospitals: List all registered hospitals
- Remove Hospital: Revoke hospital access
- Reset Password: Change password

**Key Operations:**
1. **Hospital Management**: Create and manage hospital registrations
2. **Access Control**: Manages hospital access to the system

## Security Features

### Encryption Scheme

1. **Key Generation**: 
   - Elliptic Curve (EC) key pairs using secp256r1 curve
   - Keys stored in PEM format (Base64 encoded)

2. **Key Exchange**:
   - ECDH (Elliptic Curve Diffie-Hellman) for shared secret derivation
   - SHA-256 hashing for AES key derivation

3. **Data Encryption**:
   - AES-GCM (Galois/Counter Mode) encryption
   - 12-byte IV (Initialization Vector)
   - 128-bit authentication tag
   - Format: `[IV_LENGTH (4 bytes) | IV (12 bytes) | CIPHERTEXT + TAG]`

4. **Layered Encryption**:
   - Role-based encryption (Doctor/Nurse)
   - Optional specialization-based encryption layer
   - Patient's public key used for final encryption

### Key Management

- **Public Keys**: Stored on server, used for encryption
- **Private Keys**: Stored locally in text files (`{identifier}.txt`)
- **Key Storage**: Private keys are Base64-encoded and stored as plain text files
- **Key Derivation**: ECDH shared secrets derived from EC key pairs

## File Structure

```
Cli/
├── Cli.java                 # Main entry point
├── UserCli.java            # Patient interface
├── DoctorCli.java          # Doctor interface
├── HospitalCli.java        # Hospital interface
├── AtributeAuth.java       # Attribute Authority interface
├── KeyGeneration.java      # Key generation utilities
├── EcKeyUtil.java          # EC key utilities
├── AESGCM.java             # AES-GCM encryption
├── EncryptionService.java  # Encryption wrapper
├── lib/
│   └── json.jar            # JSON library dependency
├── out/                    # Compiled classes (generated)
└── *.txt                   # Private key files (generated)
```

## API Endpoints

### User Service (Port 8090)
- `POST /user/signup` - User registration
- `POST /user/login` - User authentication
- `POST /user/logout` - User logout
- `POST /user/upload` - Upload encrypted prescription
- `POST /user/getAllDetails` - Get all user prescriptions
- `POST /user/getDetails?id={id}` - Get specific prescription
- `POST /user/deactivate` - Deactivate account
- `POST /user/setPublicKey` - Set user public key
- `POST /user/getKey?role={role}` - Get role public key
- `POST /user/getSecretKey?id={id}` - Get encryption keys for prescription

### Doctor Service (Port 8082)
- `POST /staff/login` - Doctor authentication
- `POST /staff/logout` - Doctor logout
- `POST /staff/resetPass` - Reset password
- `POST /staff/getPDetailsByHos` - Get hospital prescriptions
- `POST /staff/getPDetailsByProf` - Get accessible prescriptions
- `POST /staff/getPrescription?id={id}` - Get prescription details
- `POST /staff/getKey?id={id}` - Get encryption keys
- `POST /staff/setPublicKey` - Set doctor public key

### Hospital Service (Port 8081)
- `POST /hospital/login` - Hospital authentication
- `POST /hospital/logout` - Hospital logout
- `POST /hospital/resetPass` - Reset password
- `POST /hospital/addStaff` - Add staff member
- `POST /hospital/seeAllStaff` - View all staff
- `POST /hospital/removeStaff?regNo={regNo}` - Remove staff
- `POST /hospital/setPublicKey` - Set hospital public key

### Attribute Authority Service (Port 8080)
- `POST /AA/login` - Attribute Authority authentication
- `POST /AA/logout` - Attribute Authority logout
- `POST /AA/resetPass` - Reset password
- `POST /AA/createHospital` - Create hospital
- `POST /AA/seeAllHospital` - View all hospitals
- `POST /AA/revokeHospital?id={id}` - Revoke hospital
- `POST /AA/setPublicKey` - Set public key

## Troubleshooting

### Common Issues

1. **"Server did not respond"**
   - Ensure all backend services are running
   - Check service ports (8090, 8081, 8082, 8080)
   - Verify network connectivity

2. **"No file found for private key"**
   - Ensure you've completed signup/login process
   - Check for `{identifier}.txt` file in CLI directory
   - Re-run signup/login to regenerate keys

3. **"Invalid JSON response"**
   - Check server logs for errors
   - Verify request format matches API expectations
   - Ensure JWT token is valid (try logging in again)

4. **Compilation Errors**
   - Ensure `json.jar` is in `lib/` directory
   - Check Java version (JDK 8+)
   - Verify all source files are present

5. **Image Decryption Fails**
   - Verify private key file exists
   - Check that you have proper access (role/specialization)
   - Ensure encryption keys are correctly retrieved from server

## Security Considerations

⚠️ **Important Security Notes:**

1. **Private Key Storage**: Private keys are stored as plain text files. In production, implement secure key storage (encrypted keychain, hardware security modules).

2. **Key File Protection**: Ensure `.txt` key files are not committed to version control (add to `.gitignore`).

3. **Network Security**: All communications should use HTTPS in production environments.

4. **Password Security**: Implement strong password policies and consider password hashing improvements.

5. **Session Management**: JWT tokens should have appropriate expiration times.

## Development

### Adding New Features

1. **New CLI Command**: Add menu option and handler method in respective CLI class
2. **API Integration**: Use `sendPost()` or `sendMultipart()` methods for HTTP requests
3. **Encryption**: Use `AESGCM` class for encryption/decryption operations
4. **Key Management**: Use `KeyGeneration` and `EcKeyUtil` for key operations

### Testing

1. Start all backend services
2. Test each role's functionality:
   - User signup/login
   - Prescription upload/view
   - Doctor access to prescriptions
   - Hospital staff management
   - Attribute Authority hospital management

## Dependencies

- **JSON Library**: `org.json` (json.jar)
- **Java Standard Library**: 
  - `javax.crypto.*` - Cryptography
  - `java.security.*` - Security
  - `java.net.*` - HTTP connections
  - `java.nio.*` - File operations



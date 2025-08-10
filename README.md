# Invoice Scanner Backend

A Spring Boot REST API for invoice processing with Tesseract OCR capabilities.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher  
- PostgreSQL 12 or higher
- Tesseract OCR

## Tesseract OCR Setup

### Windows Installation

1. Download Tesseract from: https://github.com/UB-Mannheim/tesseract/wiki
2. Install to default location: `C:\Program Files\Tesseract-OCR\`
3. Add Tesseract to your system PATH (optional but recommended)
4. Verify installation: `tesseract --version`

### Additional Language Packs

For multi-language invoice processing:
1. Download language data from: https://github.com/tesseract-ocr/tessdata
2. Place in: `C:\Program Files\Tesseract-OCR\tessdata\`
3. Update `TESSERACT_LANGUAGE` config (e.g., `eng+fra+deu`)

## Setup Instructions

### 1. Environment Configuration

1. Copy the environment template:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and fill in your actual credentials:
   - Database connection details
   - JWT secret (generate a secure 256-bit key)
   - Gmail SMTP credentials for email notifications
   - Tesseract OCR paths and settings

### 2. Database Setup

1. Install PostgreSQL
2. Create a database for the application
3. Update database credentials in `.env`

### 3. Gmail Configuration

1. Enable 2-factor authentication on your Gmail account
2. Generate an app-specific password
3. Add the credentials to `.env`:
   ```
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ```

### 4. Running the Application

```bash
# Development
mvn spring-boot:run

# Production build
mvn clean package
java -jar target/invoice-scanner-backend-0.0.1-SNAPSHOT.jar
```

### 5. Testing

```bash
mvn test
```

## Security Notes

- Never commit the `.env` file to version control
- Use strong, unique passwords for all services
- Regularly rotate JWT secrets and API keys
- Keep dependencies updated

## API Documentation

The API will be available at:
- Base URL: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health`

## Tech Stack

- Spring Boot 3.5.4
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL (production)
- H2 (testing)
- Maven

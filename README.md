# HabitJourney Backend

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?style=for-the-badge&logo=postgresql)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

[Leer en español](README.es.md)

A REST API developed with Java and Spring Boot for user management and authentication.

---

## 📖 About This Project

This backend was originally developed as the authentication and user management system for the **HabitJourney** Android app, as part of my Final Project for the DAM (Multi-Platform Application Development) vocational degree.

Later, as a technical improvement exercise, the client application was decoupled from this backend and migrated to **Firebase Authentication**.

Therefore, this repository is maintained as a functional **Proof of Concept** that illustrates the creation of a REST API featuring:

* Service-oriented architecture
* User authentication based on JWT (JSON Web Tokens)
* User lifecycle management (CRUD, password change)
* Configuration for deployment in Docker containers

## 📑 Table of Contents
- [Technologies](#-technologies)
- [Prerequisites](#-prerequisites)
- [Setup and Installation](#-setup-and-installation)
- [Docker](#-docker)
- [API Documentation](#-api-documentation)
- [Project Structure](#️-project-structure)
- [Deployment](#-deployment)
- [Security](#-security)
- [Testing](#-testing)
- [License](#-license)
- [Contact](#-contact)

## 🚀 Technologies

* **Language & Framework:** Java 21, Spring Boot 3.x
* **Security:** Spring Security, JWT
* **Database:** PostgreSQL, Spring Data JPA
* **Dependency Management:** Maven
* **Containerization:** Docker & Docker Compose
* **API Documentation:** Swagger (Springdoc OpenAPI)

## 📋 Prerequisites

* JDK 21 or higher
* Maven 3.8+
* PostgreSQL 14+
* Git
* Docker (Optional, recommended for the database)

## 🔧 Setup and Installation

### 1. Clone the repository
```bash
git clone [https://github.com/Alejandro-Araujo/habitjourney-backend.git](https://github.com/Alejandro-Araujo/habitjourney-backend.git)
cd habitjourney-backend
```

### 2. Set up the database

#### Option A: Docker Compose (Recommended)
The project includes a docker-compose.yml file that sets up a PostgreSQL service.

```bash
# Start the PostgreSQL container in the background
docker-compose up -d
```

The database will be available on localhost:5433.

#### Option B: Local PostgreSQL Instance
If you prefer to use a local installation, create the database:

```sql
CREATE DATABASE habitjourney_backend;
```

### 3.  Configure environment variables
Create a .env.properties file in the project root from the env.properties.example file and adjust it with your configuration.

```properties
# Database Configuration
DB_HOST=localhost
DB_PORT=5433
DB_NAME=habitjourney_backend
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT Configuration
JWT_SECRET=a_very_secure_secret_key_for_local_development
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080
```

### 4. Run the application
```bash
# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will be available at http://localhost:8080.

## 🐳 Docker

To run the entire application inside a Docker container (assuming the database is already running):

```bash
# 1. Build the application's Docker image
docker build -t habitjourney-backend .

# 2. Run the API container
docker run -p 8080:8080 \
  --network="host" \
  -e DB_HOST=localhost \
  -e DB_PORT=5433 \
  -e DB_NAME=habitjourney_backend \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=tu_clave_secreta \
  --name habitjourney-api \
  habitjourney-backend
```

**Note:** `--network="host"` is used to easily connect to the database running on the host machine's localhost.

## 📚 API Documentation

The API documentation, generated with Swagger, is available at the following path once the application is running:

**http://localhost:8080/swagger-ui.html**

### Main Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registers a new user. |
| POST | `/api/auth/login` | Authenticates a user and returns a JWT. |
| GET | `/api/users/me` | Gets the authenticated user's data. |
| PUT | `/api/users/me` | Updates the user's data. |
| POST | `/api/users/me/change-password` | Allows the user to change their password. |
| DELETE | `/api/users/me` | Deletes the user's account. |

## 🏗️ Project Structure

```
src/main/java/backend/
├── auth/          # Authentication logic (Controller, DTOs, Service)
├── user/          # User management logic (Controller, Entity, Repo, Service)
└── common/        # Cross-cutting components
    ├── config/    # Spring configuration (Security, OpenAPI, etc.)
    ├── exception/ # Global exception handlers
    └── security/  # JWT implementation (Filters, Provider, etc.)
```
## 🚀 Deployment

This project is configured for easy deployment to platforms like Render using Docker.

### Live Demo (Render.com)

An instance of this API is deployed on Render.

- **Base URL:** `https://habitjourney-backend.onrender.com`
- **API Docs:** `https://habitjourney-backend.onrender.com/swagger-ui.html`

**Note:** This demo runs on Render's free tier. The service may take a moment to start if it has been inactive. Additionally, the associated database has a limited lifecycle and may not be permanently available.

### Deploying Your Own Instance on Render

You can deploy your own version of this API by following these steps:

1. Fork this repository.
2. Create an account on [Render.com](https://render.com).
3. Create a new "Web Service" → connect it to your forked repository.
4. Setup:
    - Environment: `Docker`
    - Build Command: `docker build -t habitjourney-backend .`
    - Start Command: `docker run -p $PORT:8080 habitjourney-backend`
5. Add the necessary environment variables (DB_HOST, JWT_SECRET, etc.). If you use Render's database, they will provide you with the DATABASE_URL.
6. Deploy! Render will automatically detect the Dockerfile, build the image, and run it.

## 🔐 Security

The following security measures have been implemented:

* **Authentication:** A system based on JSON Web Tokens (JWT) to protect endpoints.
* **Password Hashing:** BCryptPasswordEncoder is used to securely store passwords.
* **Input Validation:** DTOs (Data Transfer Objects) validate incoming data to prevent malformed inputs.
* **CORS Configuration:** A Cross-Origin Resource Sharing policy has been configured to allow requests from specific origins.

## 🧪 Testing

The project includes unit and integration tests to ensure the correct functionality of controllers and services.

To run the test suite:

```bash
mvn test
```

To generate a coverage report (requires JaCoCo):

```bash
mvn test jacoco:report
```

## 📝 License

This project is distributed under the MIT License. See the LICENSE file for more details.

## 📞 Contact

**Alejandro Araujo Fernández**

* Email: jandroaraujo@gmail.com
* LinkedIn: https://www.linkedin.com/in/alejandro-araujo-fernandez/
* GitHub: https://github.com/Alejandro-Araujo
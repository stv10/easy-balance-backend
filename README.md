# Easy Balance - Backend

Easy Balance Backend is a RESTful API built with Spring Boot and Java 25. It provides endpoints for managing accounts, budget configurations, monthly fixed expenses, variable expenses, and sends alert updates via Resend email service.

---

## 🚀 Tech Stack

- **Framework**: [Spring Boot 4.1.1](https://spring.io/projects/spring-boot)
- **Language**: Java 25
- **Database**: PostgreSQL (JPA / Hibernate)
- **Security**: Spring Security & JWT Authentication
- **Build Tool**: Maven
- **Libraries**: Lombok, Hibernate Validation, Resend Integration

---

## 🛠️ Local Development Setup

### Prerequisites

- [Java JDK 25](https://adoptium.net/temurin/releases/)
- [PostgreSQL](https://www.postgresql.org/) database running locally

### Configuration

For local development, create your local configuration file by copying the provided example:

```bash
cp src/main/resources/application-local.yaml.example src/main/resources/application-local.yaml
```

> **Note:** `application-local.yaml` is excluded by `.gitignore` and must never be committed to source control.

Edit `src/main/resources/application-local.yaml` to specify your local database password, JWT secret, and optional Resend API credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mybalance
    username: postgres
    password: YOUR_DB_PASSWORD

resend:
  apiKey: "re_YOUR_RESEND_KEY"
  toEmail: "dev@example.com"
  fromEmail: "onboarding@resend.dev"

jwt:
  secret: "YOUR_MIN_256_BITS_JWT_SECRET_KEY_HERE_FOR_LOCAL_DEV"
  expiration: 86400000

admin:
  username: "admin"
  password: "admin_password"

cors:
  allowed-origins: "http://localhost:5173"
```

### Running Locally

```bash
./mvnw spring-boot:run
```
The backend API server will start at `http://localhost:8080/`.

---

## ⚙️ Production Environment Variables

When running in production (such as **Dokploy**, Kubernetes, or Docker on a VPS), set `SPRING_PROFILES_ACTIVE=prod` and provide the following environment variables:

| Variable | Required | Description | Example / Default |
| :--- | :---: | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Yes | Active Spring profile. Set to `prod`. | `prod` |
| `DB_URL` | Yes | JDBC PostgreSQL connection URL. | `jdbc:postgresql://db:5432/mybalance` |
| `DB_USERNAME` | Yes | PostgreSQL database user. | `postgres` |
| `DB_PASSWORD` | Yes | PostgreSQL database password. | `your_secret_db_password` |
| `JWT_SECRET` | Yes | Secret key for signing JWT tokens (min 256 bits / 32+ characters). | `a_very_long_secure_secret_token_key_here` |
| `JWT_EXPIRATION` | No | Expiration time for JWT tokens in milliseconds. | `86400000` (24 hours) |
| `ADMIN_USERNAME` | Yes | Initial administrator username (seeded if DB is empty). | `admin` |
| `ADMIN_PASSWORD` | Yes | Initial administrator password (seeded if DB is empty). | `your_secure_admin_password` |
| `CORS_ALLOWED_ORIGINS` | Yes | Allowed frontend origin URLs for CORS (comma-separated). | `https://mybalance.example.com` |
| `RESEND_API_KEY` | Yes | API Key from Resend email service. | `re_123456789ABC` |
| `RESEND_TO_EMAIL` | Yes | Destination email address for alert notifications. | `alerts@example.com` |
| `RESEND_FROM_EMAIL` | Yes | Verified sender email configured in Resend. | `onboarding@resend.dev` |

---

## 🐳 Docker & Container Deployment

This repository includes a multi-stage Docker build config using OpenJDK 25 (Temurin).

### Build locally
```bash
docker build -t ghcr.io/stv10/easy-balance-backend:latest .
```

### Run Container
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/mybalance \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_db_password \
  -e JWT_SECRET=your_jwt_secret_key_minimum_256_bits_length \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=your_secure_password \
  -e CORS_ALLOWED_ORIGINS=http://localhost:5173 \
  -e RESEND_API_KEY=re_your_api_key \
  -e RESEND_TO_EMAIL=alerts@example.com \
  -e RESEND_FROM_EMAIL=onboarding@resend.dev \
  ghcr.io/stv10/easy-balance-backend:latest
```

### Pull Image from GitHub Container Registry (GHCR)
```bash
docker pull ghcr.io/stv10/easy-balance-backend:latest
```

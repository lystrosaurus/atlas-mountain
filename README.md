# atlas-mountain

Spring Boot foundation application for reusable backend capabilities.

## Baseline

- Java 21
- Spring Boot 4
- Maven
- MySQL
- Redis

## Local Services

Create a MySQL database:

```sql
CREATE DATABASE atlas_mountain DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'atlas'@'%' IDENTIFIED BY 'atlas';
GRANT ALL PRIVILEGES ON atlas_mountain.* TO 'atlas'@'%';
FLUSH PRIVILEGES;
```

Run Redis on `localhost:6379`.

## Local Run

```bash
mvn spring-boot:run
```

## Development Seed Account

Flyway creates a local development user for login verification. This account is only for local development. It is not a production bootstrap account and must not be enabled in production deployments.

Default local login:

- Username: `admin`
- Password: `atlas-local`

This credential is only for local verification. Do not use it in production.

## Verification

Run unit and architecture tests:

```bash
mvn test
```

Run locally after MySQL and Redis are available:

```bash
mvn spring-boot:run
```

Verify public endpoint:

```bash
curl http://localhost:8080/api/public/ping
```

Verify login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"atlas-local\"}"
```

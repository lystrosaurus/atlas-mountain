# atlas-mountain

Spring Boot 4 single-application backend foundation. Java 21, Maven, strict layering (ArchUnit enforced).

## Capabilities

- Sa-Token session + API Token dual authentication
- User management
- Redisson distributed locks
- MyBatis-Plus + Flyway schema management
- MySQL binlog CDC (Change Data Capture)

## Baseline

- Java 21
- Spring Boot 4
- Maven
- MySQL
- Redis

## Project Structure

```
config    # Spring beans (Jackson, MyBatis-Plus, Sa-Token, Web)
common    # ApiResponse<T>, ErrorCode, BusinessException
web       # GlobalExceptionHandler, RequestLogFilter
auth      # Auth module (controller/service/dao/dao.impl/mapper/entity/dto/vo)
user      # User module (controller/service/dao/dao.impl/mapper/entity/vo)
cdc       # MySQL binlog CDC (config/engine/dispatcher/handler/event)
infra     # persistence (BaseEntity, AuditMetaObjectHandler), redis (distributed lock)
ops       # Operations placeholder
```

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

## Formatting

Spotless enforces code style. Run before committing:

```bash
mvn spotless:apply
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

# atlas-mountain

Spring Boot 4 single-application backend foundation. Java 21, Maven, strict layering (ArchUnit enforced).

## Capabilities

- Sa-Token session + API Token dual authentication
- Login and current-user lookup
- Redisson distributed locks
- Bucket4j request rate limiting
- MyBatis-Plus + Flyway schema management
- MySQL binlog CDC (Change Data Capture)

## Authentication Boundaries

- `/api/public/**` is unauthenticated.
- `/api/open/**` requires `X-API-Token` in the `ak_<prefix>_<secret>` format.
- All other routes require a Sa-Token session, except login, actuator health, and internal error dispatch paths.

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

Keep local credentials outside the build output:

```bash
mkdir -p config
cp dev/application-local.yml.example config/application-local.yml
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The template and local seed migration live under `dev/`; neither is included in the packaged classpath.
`application-local.yml` is also excluded from Maven resources and must not be placed in the packaged classpath.

## Formatting

Spotless enforces code style. Run before committing:

```bash
mvn spotless:apply
```

## Development Seed Account

The `local` and `test` profiles load a separate Flyway location that creates a development user for login verification. Common production migrations remove the historical development seed.

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
mvn spring-boot:run -Dspring-boot.run.profiles=local
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

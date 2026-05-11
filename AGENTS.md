# AGENTS.md

This file provides guidance for AI coding agents working with the atlas-mountain codebase.

## Project Overview

Atlas Mountain is a Spring Boot 4 single-application backend foundation. It is a Maven-based monolith (not multi-module) that accumulates reusable backend capabilities separated by package boundaries and strict layering rules.

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.6 (Spring Framework 7 generation)
- **Build tool**: Maven
- **Group ID**: `io.github.lystrosaurus`
- **Artifact ID**: `atlas-mountain`
- **Base package**: `io.github.lystrosaurus.atlasmountain`
- **Database**: MySQL
- **Cache/Redis**: Redis (via Redisson)

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Web | Spring Boot Starter Web | 4.0.6 |
| Validation | Spring Boot Starter Validation | 4.0.6 |
| AOP | Spring Boot Starter AspectJ | 4.0.6 |
| ORM | MyBatis-Plus (Spring Boot 3 starter) | 3.5.16 |
| Auth | Sa-Token (Spring Boot 3 starter) | 1.45.0 |
| Redis | Redisson (Spring Boot starter) | 4.3.1 |
| DB Migration | Flyway MySQL | 12.5.0 |
| Database Driver | MySQL Connector/J | runtime |
| Crypto | spring-security-crypto | 7.0.5 (BCrypt only) |
| Ops | Spring Boot Actuator | 4.0.6 |
| Testing | JUnit 5, Mockito, AssertJ | via spring-boot-starter-test |
| Architecture Testing | ArchUnit JUnit 5 | 1.4.2 |

## Build and Test Commands

```bash
# Compile and run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ApiResponseTest

# Run multiple specific test classes
mvn test -Dtest=ApiTokenServiceImplTest,AuthServiceImplTest

# Run the application locally (requires MySQL and Redis)
mvn spring-boot:run

# Compile only
mvn compile
```

## Local Development Setup

MySQL and Redis are developer-provided local services. No Testcontainers is used.

**MySQL setup**:
```sql
CREATE DATABASE atlas_mountain DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'atlas'@'%' IDENTIFIED BY 'atlas';
GRANT ALL PRIVILEGES ON atlas_mountain.* TO 'atlas'@'%';
FLUSH PRIVILEGES;
```

**Redis**: Run on `localhost:6379`.

**Local profile**: `application-local.yml` is gitignored and must be created by each developer. It contains sensitive datasource and Redis credentials. The active profile is set to `local` in `application.yml`.

## Code Organization

```text
io.github.lystrosaurus.atlasmountain
├── AtlasMountainApplication.java    # Entry point
├── config                           # Spring bean assembly & framework config
│   ├── JacksonConfig.java
│   ├── MybatisPlusConfig.java
│   ├── SaTokenConfig.java
│   └── WebConfig.java
├── common                           # Cross-cutting primitives
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── CommonErrorCode.java
│   │   └── ErrorCode.java
│   └── response/
│       └── ApiResponse.java
├── web                              # HTTP runtime
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   └── log/
│       └── RequestLogFilter.java
├── auth                             # Authentication & API token business
│   ├── controller/
│   ├── dao/ + dao/impl/
│   ├── dto/
│   ├── entity/
│   ├── mapper/
│   ├── service/ + service impls
│   └── vo/
├── user                             # User business
│   ├── controller/
│   ├── dao/ + dao/impl/
│   ├── entity/
│   ├── mapper/
│   ├── service/ + service impls
│   └── vo/
├── infra                            # Infrastructure concerns
│   ├── persistence/
│   │   ├── BaseEntity.java
│   │   └── AuditMetaObjectHandler.java
│   └── redis/
│       ├── DistributedLock.java
│       ├── DistributedLockAspect.java
│       ├── DistributedLockKeyResolver.java
│       ├── DistributedLockService.java
│       └── RedissonDistributedLockService.java
└── ops                              # Operational endpoints (placeholder)
    └── package-info.java
```

## Architecture Rules

### Strict Layering (Enforced by ArchUnit)

```text
Controller -> Service -> DAO -> DAO Impl -> Mapper -> Database
```

Key rules:
- Controllers only handle HTTP I/O. They depend on services, DTOs, VOs, and common response types. **Controllers must NOT depend on DAOs, mappers, or entities.**
- Services contain business workflows. They depend on DAOs and other services. **Services must NOT depend on MyBatis-Plus mappers directly.**
- DAO interfaces define data access operations used by services.
- DAO implementations are the **only** feature-layer classes that depend on MyBatis-Plus mappers.
- Mappers are treated as generation-compatible persistence adapters.
- Entities model database tables and must **not** be used as public API response contracts.
- DTOs model incoming requests and commands; VOs model API responses.

ArchUnit enforces these rules in `src/test/java/.../architecture/LayerArchitectureTest.java`.

### Authentication Design

Three endpoint categories with distinct path prefixes:

| Category | Path Pattern | Auth Method |
|----------|-------------|-------------|
| Public | `/api/public/**` | None |
| Open API | `/api/open/**` | `X-API-Token` header (prefix + SHA-256 hash lookup) |
| Logged-in | `/api/app/**` | Sa-Token login session |

Sa-Token route checks are configured in `SaTokenConfig` via interceptors.

### API Token Format

Tokens use a split format: `ak_<prefix>_<secret>`
- `prefix` is a short public lookup key stored in `api_token.token_prefix` (unique column).
- `secret` is never stored in plain text.
- The server hashes the full presented token with SHA-256 and compares it to `api_token.token_hash`.

### Distributed Lock Design

Redisson provides both programmatic and annotation-based locking:

- **Programmatic API**: `DistributedLockService.execute(key, waitTime, leaseTime, timeUnit, action)` — fail-fast with `BusinessException` on lock failure.
- **Annotation API**: `@DistributedLock(key = "demo:#{#resourceId}", waitTime = 1, leaseTime = 5)` — SpEL expressions supported, backed by Spring AOP around advice.

### Persistence

- **Database**: MySQL with Flyway schema migration (`src/main/resources/db/migration/`).
- **ORM**: MyBatis-Plus with pagination interceptor.
- **Audit**: `AuditMetaObjectHandler` fills `createdAt`, `createdBy`, `updatedAt`, `updatedBy` automatically.
- **Soft delete**: MyBatis-Plus `@TableLogic` on `deleted` field.
- **Base entity**: `BaseEntity` in `infra.persistence` provides common audit and soft-delete fields.

### Password Hashing

Passwords are encoded with BCrypt via `spring-security-crypto`'s `BCryptPasswordEncoder`. The full Spring Security web stack is intentionally **not** introduced — only the crypto utility is used.

## Code Style Guidelines

- **Constructor injection** is used exclusively (no `@Autowired` on fields).
- **Java Records** are used for DTOs and VOs where appropriate (e.g., `ApiResponse<T>`, `LoginVo`, `CurrentUserVo`).
- **Traditional POJOs** with getters/setters are used for entities extending `BaseEntity` (MyBatis-Plus compatibility).
- **Package-private test classes** — test classes do not use the `public` modifier.
- **Modern Java features**: switch expressions and pattern matching for `instanceof` are used.
- **`var` is NOT used** — explicit types throughout.
- **No Lombok** — all boilerplate is handwritten.
- **Consistent naming**: `*Service`/`*ServiceImpl`, `*Dao`/`*DaoImpl`, `*Mapper`, `*Entity`, `*Vo`, `*Dto`.

## Testing Strategy

- **Unit tests**: Mocked service logic (e.g., `AuthServiceImplTest`, `ApiTokenServiceImplTest`).
- **MVC tests**: Controller and exception handling with Spring MVC Test (e.g., `GlobalExceptionHandlerTest`).
- **Architecture tests**: ArchUnit layer dependency rules (`LayerArchitectureTest.java`).
- **Guard tests**: `DependencyDecisionTest` verifies the dependency decision doc has no placeholder text.
- **Infrastructure tests**: Distributed lock key parsing and service behavior with mocked Redisson.
- **No Testcontainers**: MySQL and Redis integration is verified manually against local services.

## Security Considerations

- Do not add Spring Security web stack — use `spring-security-crypto` only for BCrypt.
- Do not bypass the DAO layer — services must not depend on mappers directly.
- Do not use entities as API response contracts — use VOs instead.
- The development seed account (`admin`/`atlas-local`) created by Flyway is **local-only** and must never be enabled in production.
- `application-local.yml` is gitignored and contains sensitive credentials. Each developer maintains their own.

## Key Design Documents

- `docs/superpowers/specs/2026-05-09-atlas-mountain-spring-boot-design.md` — Full architecture spec
- `docs/superpowers/plans/2026-05-09-atlas-mountain-spring-boot.md` — Step-by-step implementation plan
- `docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md` — Verified dependency compatibility decisions

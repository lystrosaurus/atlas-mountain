# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Atlas Mountain is a Spring Boot 4 single-application backend foundation. It is a Maven project (not multi-module) that accumulates reusable backend capabilities separated by package boundaries and strict layering rules.

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.6 (Spring Framework 7 generation)
- **Build tool**: Maven
- **Group ID**: `io.github.lystrosaurus`
- **Artifact ID**: `atlas-mountain`
- **Base package**: `io.github.lystrosaurus.atlasmountain`
- **Server port**: 8080

## Build and Development Commands

```bash
# Compile and run all tests (no local services required)
mvn test

# Run a specific test class
mvn test -Dtest=ApiResponseTest

# Run multiple specific test classes
mvn test -Dtest=ApiTokenServiceImplTest,AuthServiceImplTest

# Compile only
mvn compile

# Run the application locally (requires MySQL and Redis)
mvn spring-boot:run
```

## Local Services Setup

MySQL and Redis are developer-provided local services (no Testcontainers).

**MySQL setup**:
```sql
CREATE DATABASE atlas_mountain DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'atlas'@'%' IDENTIFIED BY 'atlas';
GRANT ALL PRIVILEGES ON atlas_mountain.* TO 'atlas'@'%';
FLUSH PRIVILEGES;
```

**Redis**: Run on `localhost:6379`.

**Local profile**: `application-local.yml` is gitignored and must be created by each developer. It is the active profile by default (`spring.profiles.active: local` in `application.yml`) and contains sensitive datasource and Redis credentials.

**Local verification** (after `mvn spring-boot:run`):
```bash
# Public endpoint
curl http://localhost:8080/api/public/ping

# Login with the development seed account
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"atlas-local\"}"
```

## High-Level Architecture

### Package Structure and Responsibilities

```text
io.github.lystrosaurus.atlasmountain
├── config          # Spring bean assembly and third-party framework configuration
├── common          # Cross-cutting primitives: ApiResponse, ErrorCode, BusinessException
├── web             # HTTP runtime: global exception handling, request logging, validation
├── auth            # Authentication and API token business behavior
├── user            # User business behavior
├── infra           # Infrastructure concerns
│   ├── persistence # MyBatis-Plus pagination, audit fields, base entity
│   └── redis       # Redisson distributed lock service, annotation, AOP advice
└── ops             # Operational endpoints (Actuator, etc.)
```

### Strict Layering Rules

Feature packages (`auth`, `user`) enforce Controller -> Service -> DAO -> DAO Impl -> Mapper layering. ArchUnit tests prevent layer bypasses.

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

Sa-Token route checks are configured in `SaTokenConfig` via interceptors. The first version does **not** include a full RBAC system.

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

### Serialization and Actuator

- Jackson is configured with `default-property-inclusion: non_null`, so null fields are omitted from JSON responses.
- Spring Boot Actuator exposes `health` and `info` endpoints with details visible.

## Testing Strategy

- **Unit tests**: Mocked service logic (API token verification, distributed lock key parsing).
- **MVC tests**: Controller and exception handling with Spring MVC Test.
- **Architecture tests**: ArchUnit layer dependency rules.
- **No Testcontainers**: MySQL and Redis integration is verified manually against local services. The test suite does not require local services to run.

## Key Design Documents

- `docs/superpowers/specs/2026-05-09-atlas-mountain-spring-boot-design.md` — Full architecture spec
- `docs/superpowers/plans/2026-05-09-atlas-mountain-spring-boot.md` — Step-by-step implementation plan
- `docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md` — Verified dependency compatibility decisions

## Important Constraints

- Do not add Spring Security web stack — use `spring-security-crypto` only for BCrypt.
- Do not bypass the DAO layer — services must not depend on mappers directly.
- Do not use entities as API response contracts — use VOs instead.
- The development seed account (`admin`/`atlas-local`) created by Flyway is **local-only** and must never be enabled in production.

# Atlas Mountain Spring Boot Design

## Context

Atlas Mountain is a new GitHub-hosted Spring Boot application for accumulating reusable backend capabilities. The first version is a single Maven application, not a multi-module framework. Capabilities are separated by package boundaries and strict layering rules so the codebase can grow without losing structure.

## Technical Baseline

- Language: Java 21
- Framework: Spring Boot 4.0.6
- Build tool: Maven
- Group ID: `io.github.lystrosaurus`
- Artifact ID: `atlas-mountain`
- Base package: `io.github.lystrosaurus.atlasmountain`
- Database: MySQL, configured by the developer in local environment files
- Persistence: MyBatis-Plus
- Authentication: Sa-Token
- Redis client: Redisson
- Schema migration: Flyway
- Operations: Spring Boot Actuator
- Layer rules: enforced by ArchUnit tests
- External integration tests: no Testcontainers in the first version

## Dependency Compatibility

Spring Boot 4 uses the Spring Framework 7 generation, so the first implementation step is a dependency compatibility spike before application code is built.

Required checks:

- Confirm the latest stable MyBatis-Plus artifacts work with Spring Boot 4 and Spring Framework 7.
- Confirm the latest stable Sa-Token Spring Boot starter works with Spring Boot 4.
- Confirm the latest stable Redisson Spring Boot integration works with Spring Boot 4.
- Confirm ArchUnit works with the selected Java 21 test stack.
- Confirm Flyway supports the selected MySQL version and Java 21 runtime.
- Record the verified versions and decisions in an implementation dependency table before feature work starts.

Fallback rules:

- If a Spring Boot starter is not Boot 4 compatible, use the library's plain core artifact and create explicit Spring configuration in `config`.
- If MyBatis-Plus cannot run cleanly on Spring Boot 4, pause implementation and choose between plain MyBatis, Spring Data JDBC, or downgrading Spring Boot. Do not silently replace it.
- If Sa-Token cannot run cleanly on Spring Boot 4, pause implementation and choose between explicit Sa-Token configuration, Spring Security, or downgrading Spring Boot. Do not silently replace it.
- If Redisson's starter is incompatible, use `redisson` core with a manually created `RedissonClient` bean.
- Exact artifact versions must be recorded in `pom.xml` during implementation.
- The compatibility spike output must be committed as `docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md` before feature implementation begins.

## Architecture

The application is a single deployable Spring Boot service. It exposes HTTP APIs, uses Sa-Token for lightweight authentication, uses MyBatis-Plus for database access, and uses Redisson for distributed locks.

The design favors a small but complete runtime loop:

- Public endpoints work without credentials.
- Open API endpoints require an issued API token.
- Application endpoints require a logged-in Sa-Token session.
- User and token data are stored in MySQL.
- Distributed lock behavior is available through a service API and method annotation.
- Health and runtime information are exposed through Actuator.

## Package Structure

```text
io.github.lystrosaurus.atlasmountain
├── AtlasMountainApplication
├── config
│   ├── WebConfig
│   ├── SaTokenConfig
│   ├── MybatisPlusConfig
│   ├── RedissonConfig
│   └── JacksonConfig
├── common
│   ├── exception
│   ├── response
│   └── support
├── web
│   ├── exception
│   ├── log
│   └── validation
├── auth
│   ├── controller
│   ├── service
│   ├── dao
│   │   └── impl
│   ├── mapper
│   ├── entity
│   ├── dto
│   └── vo
├── user
│   ├── controller
│   ├── service
│   ├── dao
│   │   └── impl
│   ├── mapper
│   ├── entity
│   ├── dto
│   └── vo
├── infra
│   ├── persistence
│   └── redis
└── ops
```

## Package Responsibilities

`config` contains Spring bean assembly and third-party framework configuration. It wires capabilities into the application context but does not contain business behavior.

`common` contains cross-cutting primitives: unified API responses, error codes, business exceptions, constants, and small support utilities.

`web` contains HTTP runtime behavior: global exception handling, request logging, validation support, and interceptor implementations.

`auth` contains authentication and API token business behavior. It owns login, logout, current session lookup, and open API token verification.

`user` contains user business behavior. The first version includes the minimal user model required for login and current-user examples.

`infra.persistence` contains database infrastructure such as MyBatis-Plus pagination, audit field filling, base persistence support, and mapper scanning support when needed.

`infra.redis` contains Redisson-backed infrastructure: distributed lock service, lock annotation, AOP advice, key parsing, and lock failure handling.

`ops` contains operational endpoints or support classes that are not core business features.

## Layering Rules

Feature packages use strict controller-service-DAO layering.

```text
Controller -> Service -> DAO -> DAO Impl -> Mapper -> Database
```

Rules:

- Controllers only handle HTTP input and output. They may depend on services, DTOs, VOs, and common response types.
- Controllers must not depend on DAO interfaces, DAO implementations, mappers, or entities.
- Services contain business workflows. They may depend on DAOs, other services, common types, and infrastructure services.
- Services must not depend on MyBatis-Plus mappers directly.
- DAO interfaces define data access operations used by services.
- DAO implementations are the only feature-layer classes that depend on MyBatis-Plus mappers.
- Mappers are the MyBatis-Plus persistence adapters. They are treated as generated or generation-compatible code.
- Entities model database tables and should not be used as public API response contracts.
- DTOs model incoming requests and commands.
- VOs model API responses.

ArchUnit tests enforce the key dependency rules:

- `..controller..` must not depend on `..dao..` or `..mapper..`.
- `..controller..` must not depend on `..entity..`.
- `..service..` must not depend on `..mapper..`.
- Only `..dao.impl..` may depend on `..mapper..` in feature packages.

## Authentication Design

The first version supports three endpoint categories.

Public endpoints:

- Path pattern: `/api/public/**`
- Login path: `/api/auth/login`
- Health path: `/actuator/health`
- No authentication required.

Open API token endpoints:

- Path pattern: `/api/open/**`
- Credential header: `X-API-Token`
- Token values are stored as hashes, not plain text.
- Expired or disabled tokens fail with a unified business error.

Logged-in application endpoints:

- Path pattern: `/api/app/**`
- Authentication: Sa-Token login session.
- The current user ID is read from Sa-Token context.

The first version does not include a full RBAC system. Role, permission, and menu models can be added later without changing the public/open/app endpoint split.

## Initial Data Model

`sys_user` stores the minimal login user model:

- `id`
- `username`
- `password_hash`
- `nickname`
- `status`
- `created_at`
- `created_by`
- `updated_at`
- `updated_by`
- `deleted`

`api_token` stores issued open API tokens:

- `id`
- `name`
- `token_prefix`
- `token_hash`
- `status`
- `expires_at`
- `created_at`
- `created_by`
- `updated_at`
- `updated_by`
- `deleted`

Both tables use MyBatis-Plus logical delete and audit field support.

Schema changes are managed by Flyway. The first implementation commits `src/main/resources/db/migration/V1__init_schema.sql` with the `sys_user` and `api_token` tables, indexes, and seed data required for local login verification.

Initial indexes:

- `sys_user.username` is globally unique. Usernames are not reused after logical deletion in the first version.
- `api_token.token_prefix` is globally unique. Prefixes are not reused after logical deletion.
- `api_token.token_hash` is globally unique. Token hashes are not reused after logical deletion.

These are plain MySQL unique indexes. The first version avoids active-only uniqueness because it complicates logical deletion semantics and generated schema portability.

Passwords are encoded with BCrypt through `spring-security-crypto`'s `BCryptPasswordEncoder`. The full Spring Security web stack is not introduced only for password hashing. The first local user is created by Flyway seed SQL with a documented development-only password. This seed account is for local development only; README must clearly state that it is not a production bootstrap account and must not be enabled in production deployments.

API tokens use a split format:

```text
ak_<prefix>_<secret>
```

Lookup and verification:

- `prefix` is a short public lookup key stored in unique column `api_token.token_prefix`.
- `secret` is never stored in plain text.
- The server hashes the full presented token with SHA-256 and compares it to `api_token.token_hash`.
- Token comparisons use exact hash matching.
- Disabled, expired, missing, or malformed tokens fail with a unified business exception.

## Initial API Surface

Public examples:

- `GET /api/public/ping`
- `POST /api/auth/login`

Open API examples:

- `GET /api/open/ping`

Logged-in examples:

- `GET /api/app/me`
- `GET /api/app/ping`

Lock example:

- `POST /api/app/locks/demo/{resourceId}`

These endpoints exist to verify the foundation. They should stay small and avoid becoming a business product surface.

## Redisson Distributed Lock Design

The Redisson capability includes both programmatic and annotation-based locking.

Programmatic API:

- `DistributedLockService`
- Executes a callback while holding a named lock.
- Supports wait time and lease time.
- Throws a business exception when the lock cannot be acquired.

Annotation API:

- `@DistributedLock`
- Supports a lock key expression.
- Supports wait time and lease time attributes.
- Uses Spring AOP around advice.
- Supports simple SpEL expressions such as `user:#{#userId}`.

Default failure behavior is fail-fast with a unified business exception. Later versions can add skip, queue, or custom fallback behavior.

## Configuration

The first version provides:

- `application.yml` for shared defaults.
- `application-local.yml` for developer-provided MySQL and Redis settings.
- README instructions for local configuration.

Configuration areas:

- server port and application name
- MySQL datasource
- MyBatis-Plus behavior
- Sa-Token session behavior
- Redisson single-server Redis connection
- Actuator health and info exposure
- logging levels

Sensitive local values are not committed.

## Testing Strategy

The first version does not use Testcontainers. MySQL and Redis are developer-provided local services.

Automated tests cover:

- unified response objects
- business exception mapping
- API token verification logic with mocked DAO dependencies
- Sa-Token-facing service logic where practical
- distributed lock key parsing
- distributed lock service behavior with mocked Redisson dependencies
- controller and exception handling with Spring MVC tests
- layer dependency rules with ArchUnit

Manual/local verification covers:

- application startup with local MySQL and Redis configuration
- MyBatis-Plus mapper access
- login flow
- open API token flow
- Redisson lock execution against a real Redis instance

## Non-Goals For First Version

- Multi-module Maven project
- Full RBAC with roles, permissions, and menus
- OAuth2 authorization server
- Testcontainers-based integration tests
- Production deployment manifests
- Admin UI or frontend application
- Code generator UI

## Evolution Path

The package structure leaves room for later capabilities:

- RBAC can extend `auth` with role and permission packages.
- Code generation can create entity, mapper, DAO, service, controller, DTO, and VO classes following the same layer rules.
- Caching and rate limiting can extend `infra.redis`.
- More operational endpoints can extend `ops`.
- If reuse pressure becomes high, common infrastructure can later be extracted to starters or modules.

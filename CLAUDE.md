# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

Atlas Mountain is a Spring Boot 4 single-application backend foundation. Java 21, Maven, strict layering.

- **Base package**: `io.github.lystrosaurus.atlasmountain`
- **Port**: 8080

## Agent Behavior Principles

Derived from Andrej Karpathy's observations on LLM coding pitfalls. These rules take precedence over speed when they conflict.

### 1. Think Before Coding
State assumptions explicitly. When uncertain, ask — don't guess. Present alternatives when ambiguity exists. Push back if a simpler path is available.

### 2. Simplicity First
Minimum code that solves the problem. No speculative features, no abstraction for single-use code, no "flexibility" that wasn't requested. If 200 lines could be 50, rewrite.

### 3. Surgical Changes
Touch only what you must. Don't "improve" adjacent code, comments, or formatting. Match existing style. Remove only imports / variables / functions that YOUR changes made unused. Mention pre-existing dead code — don't delete it.

### 4. Goal-Driven Execution
Transform imperative tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a reproducing test, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For this project: state a brief plan for multi-step tasks; run `mvn test` before claiming completion; run `mvn spotless:apply` before committing.

## Common Commands

```bash
mvn test                          # Run all tests
mvn test -Dtest="*IntegrationTest" # Run only integration tests
mvn spotless:apply                # Auto-fix formatting
mvn spring-boot:run               # Run locally (needs MySQL + Redis)
```

## Local Services

- **MySQL**: `atlas_mountain` (dev), `atlas_mountain_test` (integration tests)
- **Redis**: `localhost:6379`
- **Local profile**: `application-local.yml` (gitignored, create your own)

## Architecture

```
io.github.lystrosaurus.atlasmountain
├── config    # Spring beans
├── common    # ApiResponse, ErrorCode, BusinessException
├── web       # Filters, exception handlers
├── auth      # Auth feature (controller/service/dao/dao.impl/mapper/entity/dto/vo)
├── user      # User feature (controller/service/dao/dao.impl/mapper/entity/vo)
├── cdc       # MySQL binlog CDC (config/engine/dispatcher/handler/event)
└── infra     # persistence (MyBatis-Plus), redis (Redisson lock)
```

**Layer rules** (enforced by ArchUnit):
- Controller -> Service -> DAO -> DAO Impl -> Mapper
- Controllers must NOT depend on DAOs/mappers/entities
- Services must NOT depend on mappers directly
- Entities must NOT be used as API response contracts
- `cdc` package is exempt from layer rules — it is triggered by binlog events, not HTTP requests

**Auth**: Three endpoint prefixes — `/api/public/**` (none), `/api/open/**` (X-API-Token), `/api/app/**` (Sa-Token session).

**Lombok**: Allowed for Entity `@Getter`/`@Setter`, `@Slf4j`, `@RequiredArgsConstructor`. DTO/VO must remain plain records.

## Testing

| Type | Location | Dependencies |
|------|----------|-------------|
| Unit | `.../service/` | Mockito only |
| MVC | `.../web/` | Spring MVC Test |
| Arch | `.../architecture/` | ArchUnit |
| Integration | `.../integration/` | MySQL + Redis |

Integration test base: `IntegrationTestBase` (Flyway clean+migrate per class). MockMvc tests extend `MockMvcIntegrationTest` with `mockMvc` and `jdbcTemplate`.

## Standards

See `CODING_STANDARDS.md` for code style, naming, API design, database, and security rules.

## Key Constraints

- No Spring Security web stack (only `spring-security-crypto` for BCrypt)
- No DAO bypass — services use DAOs, not mappers
- No entities in API responses — use VOs
- Lombok limited to Entity getters/setters, `@Slf4j`, `@RequiredArgsConstructor`
- Dev seed account (`admin`/`atlas-local`) is **local-only**

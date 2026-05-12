# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

Atlas Mountain is a Spring Boot 4 single-application backend foundation. Java 21, Maven, strict layering.

- **Base package**: `io.github.lystrosaurus.atlasmountain`
- **Port**: 8080

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
├── user      # User feature (same structure)
└── infra     # persistence (MyBatis-Plus), redis (Redisson lock)
```

**Layer rules** (enforced by ArchUnit):
- Controller -> Service -> DAO -> DAO Impl -> Mapper
- Controllers must NOT depend on DAOs/mappers/entities
- Services must NOT depend on mappers directly
- Entities must NOT be used as API response contracts

**Auth**: Three endpoint prefixes — `/api/public/**` (none), `/api/open/**` (X-API-Token), `/api/app/**` (Sa-Token session).

## Testing

| Type | Location | Dependencies |
|------|----------|-------------|
| Unit | `.../service/` | Mockito only |
| MVC | `.../web/` | Spring MVC Test |
| Arch | `.../architecture/` | ArchUnit |
| Integration | `.../integration/` | MySQL + Redis |

Integration test base: `IntegrationTestBase` (Flyway clean+migrate per class). MockMvc tests extend `MockMvcIntegrationTest` with `setSaTokenContext()` for Sa-Token mock setup.

## Standards

See `CODING_STANDARDS.md` for code style, naming, API design, database, and security rules.

## Key Constraints

- No Spring Security web stack (only `spring-security-crypto` for BCrypt)
- No DAO bypass — services use DAOs, not mappers
- No entities in API responses — use VOs
- Dev seed account (`admin`/`atlas-local`) is **local-only**

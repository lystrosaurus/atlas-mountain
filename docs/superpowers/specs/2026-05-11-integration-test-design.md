# Atlas Mountain Integration Test Design

## Context

Atlas Mountain currently has unit tests and ArchUnit architecture tests but no integration tests. The test suite runs without local services because all tests use mocks. This design adds integration tests that spin up the full Spring Boot context, connect to real MySQL and Redis, and verify end-to-end request/response behavior across all endpoint categories.

## Goals

- Verify Controller -> Service -> DAO -> Mapper -> Database full stack works correctly
- Validate Sa-Token interceptor behavior, API token authentication, and global exception handling in an integrated runtime
- Verify distributed lock concurrency behavior against real Redis
- Maintain fast execution by using MockMvc for most tests and TestRestTemplate only for critical HTTP session paths
- Isolate test data from local development database

## Non-Goals

- Do not introduce Testcontainers — continue using developer-provided local services
- Do not replace existing unit tests — integration tests are complementary
- Do not add integration tests for infrastructure without HTTP endpoints (e.g., persistence layer standalone)

## Technical Approach

### Hybrid Strategy: MockMvc + TestRestTemplate

| Tool | Coverage | Rationale |
|------|----------|-----------|
| MockMvc | All standard endpoint tests | Faster, code is cleaner, Sa-Token interceptors and exception handling work identically |
| TestRestTemplate | Auth login-to-session flow only | Verifies real HTTP cookie/session propagation across requests |

Both tools run under `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `test` profile active.

### Test Configuration

**File**: `src/test/resources/application-test.yml`

```yaml
spring:
  application:
    name: atlas-mountain
  datasource:
    url: jdbc:mysql://localhost:3306/atlas_mountain_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379
  flyway:
    enabled: true

server:
  port: 0

logging:
  level:
    root: warn
    io.github.lystrosaurus.atlasmountain: debug
```

**Database isolation**: Tests use `atlas_mountain_test` database, separate from `atlas_mountain` used for local development. Developer must create it once:

```sql
CREATE DATABASE atlas_mountain_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
GRANT ALL PRIVILEGES ON atlas_mountain_test.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

### Test Base Classes

**IntegrationTestBase** — common setup:
```java
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {
}
```

`@DirtiesContext(AFTER_CLASS)` ensures each test class gets a fresh Spring context, preventing Sa-Token in-memory session leakage between test classes.

**MockMvcIntegrationTest** — for MockMvc-based tests:
```java
@AutoConfigureMockMvc
public abstract class MockMvcIntegrationTest extends IntegrationTestBase {
    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbcTemplate;
}
```

**RestTemplateIntegrationTest** — for TestRestTemplate-based tests:
```java
public abstract class RestTemplateIntegrationTest extends IntegrationTestBase {
    @Autowired protected TestRestTemplate restTemplate;
    @LocalServerPort protected int port;
    @Autowired protected JdbcTemplate jdbcTemplate;
}
```

### Data Management Strategy

**Pre-loaded data** (from Flyway V1 migration):
- `admin` user with password `atlas-local`
- Both `sys_user` and `api_token` tables exist with full schema

**Test-specific data**:
- API token tests insert token rows via `JdbcTemplate` in `@BeforeEach`
- All inserted test data is deleted in `@AfterEach` via `JdbcTemplate`
- `@Transactional` rollback is **not** used because HTTP requests execute in separate threads where the transaction boundary does not propagate

**Sa-Token session cleanup**:
- Auth-related tests call `StpUtil.logout()` in `@AfterEach` to clear in-memory sessions and prevent cross-test interference

**Redis key cleanup**:
- Distributed lock tests record exact keys used during the test
- `@AfterEach` deletes each key individually via `redissonClient.getBucket(key).delete()`
- Pattern-based deletion (`deleteByPattern`) is avoided to prevent accidental data loss

## Test Classes

### PingIntegrationTest (MockMvc)

Tests the three endpoint categories with their respective authentication requirements.

| Endpoint | Auth | Expected |
|----------|------|----------|
| `GET /api/public/ping` | None | 200, success response |
| `GET /api/open/ping` | Missing X-API-Token | 401, unauthorized |
| `GET /api/open/ping` | Valid X-API-Token | 200, success response |
| `GET /api/app/ping` | No session | 401, unauthorized |
| `GET /api/app/ping` | Valid Sa-Token session | 200, success response |

For the Sa-Token session test, the test logs in programmatically (`StpUtil.login(1L)`) and passes the token in the request header (`satoken: <token>`).

### AuthIntegrationTest (TestRestTemplate)

Verifies the complete login-to-session flow using real HTTP:

1. `POST /api/auth/login` with `admin`/`atlas-local` → 200 + Sa-Token cookie
2. `GET /api/app/ping` with session cookie → 200
3. `GET /api/app/ping` without cookie → 401

### ApiTokenIntegrationTest (MockMvc)

Tests API token verification edge cases. Inserts test tokens via `JdbcTemplate` before each test.

| Scenario | Expected |
|----------|----------|
| Malformed token (missing `ak_` prefix) | 401 |
| Token prefix not found in DB | 401 |
| Token hash mismatch | 401 |
| Token expired (`expires_at` in past) | 401 |
| Valid token | 200 |

### UserIntegrationTest (MockMvc)

| Scenario | Expected |
|----------|----------|
| `GET /api/user/me` without login | 401 |
| `GET /api/user/me` after login | 200 + current user data |

Login for MockMvc tests:
1. Call `StpUtil.login(1L)` to create an in-memory session
2. Extract token: `String token = StpUtil.getTokenValue()`
3. Pass token in request header: `.header("satoken", token)`

### DistributedLockIntegrationTest (MockMvc)

Requires real Redis. Tests the programmatic API (`DistributedLockService`).

**Scenario 1 — Single thread acquires lock successfully:**
- Call `lockService.execute("test-resource", 1, 5, SECONDS, () -> "done")`
- Assert result equals `"done"`

**Scenario 2 — Concurrent access, one succeeds, one fails:**
- Thread A acquires lock on `"concurrent-resource"` and holds for 3 seconds
- Thread B attempts to acquire same lock after 100ms delay
- Assert A returns `"A-done"`, B throws `BusinessException` with `CommonErrorCode.LOCK_BUSY`
- Uses `CountDownLatch` for synchronization and `ExecutorService` with fixed 2 threads

**Scenario 3 — Lock auto-releases after lease time:**
- Acquire lock with `leaseTime = 2` seconds
- Wait 3 seconds
- Acquire same lock again — should succeed
- Assert both acquisitions return expected values

## Assertion Patterns

**MockMvc** (using AssertJ + jsonPath):
```java
mockMvc.perform(get("/api/public/ping"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.code").value("0"))
    .andExpect(jsonPath("$.message").value("success"));
```

**TestRestTemplate** (using AssertJ):
```java
ResponseEntity<ApiResponse<LoginVo>> response = restTemplate.exchange(
    "/api/auth/login",
    HttpMethod.POST,
    new HttpEntity<>(new LoginRequest("admin", "atlas-local")),
    new ParameterizedTypeReference<>() {}
);
assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
assertThat(response.getBody().getCode()).isEqualTo("0");
```

## Important Constraints

- Integration tests require MySQL (`atlas_mountain_test` database) and Redis running on localhost
- Tests must not pollute the `atlas_mountain` development database
- `@DirtiesContext(AFTER_CLASS)` is required because Sa-Token sessions are stored in memory
- Redis key cleanup must use exact key deletion, not pattern-based deletion
- Do not use `@Transactional` for rollback — HTTP requests execute outside the test transaction

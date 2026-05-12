# Integration Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add integration tests that spin up the full Spring Boot context and verify end-to-end behavior against real MySQL and Redis.

**Architecture:** MockMvc for most endpoint tests (faster, cleaner); TestRestTemplate only for auth cookie/session flow (verifies real HTTP propagation). Tests use `test` profile with isolated `atlas_mountain_test` database.

**Tech Stack:** Spring Boot Test, MockMvc, TestRestTemplate, JUnit 5, AssertJ, MySQL, Redis

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `src/test/resources/application-test.yml` | Create | Test profile configuration: test DB, Redis, logging |
| `src/test/java/.../integration/IntegrationTestBase.java` | Create | Common base: `@SpringBootTest`, `@ActiveProfiles("test")`, `@DirtiesContext` |
| `src/test/java/.../integration/MockMvcIntegrationTest.java` | Create | MockMvc base class with `mockMvc` and `jdbcTemplate` |
| `src/test/java/.../integration/RestTemplateIntegrationTest.java` | Create | TestRestTemplate base class with `restTemplate`, `port`, `jdbcTemplate` |
| `src/test/java/.../integration/PingIntegrationTest.java` | Create | Tests all three endpoint categories (public/open/app) |
| `src/test/java/.../integration/AuthIntegrationTest.java` | Create | TestRestTemplate-based login-to-session flow |
| `src/test/java/.../integration/ApiTokenIntegrationTest.java` | Create | API token edge cases: malformed, missing, mismatch, expired, valid |
| `src/test/java/.../integration/UserIntegrationTest.java` | Create | User endpoint: unauthorized vs. authenticated |
| `src/test/java/.../integration/DistributedLockIntegrationTest.java` | Create | Redis lock: single thread, concurrent competition, auto-release |
| `CLAUDE.md` | Modify | Add integration test section |

---

## Prerequisites

Before running integration tests, create the test database:

```sql
CREATE DATABASE atlas_mountain_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
GRANT ALL PRIVILEGES ON atlas_mountain_test.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

Ensure MySQL and Redis are running on localhost.

---

### Task 1: Test Configuration and Base Classes

**Files:**
- Create: `src/test/resources/application-test.yml`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/IntegrationTestBase.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/MockMvcIntegrationTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/RestTemplateIntegrationTest.java`

- [ ] **Step 1: Create `application-test.yml`**

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

- [ ] **Step 2: Create `IntegrationTestBase.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {
}
```

- [ ] **Step 3: Create `MockMvcIntegrationTest.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class MockMvcIntegrationTest extends IntegrationTestBase {
    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbcTemplate;
}
```

- [ ] **Step 4: Create `RestTemplateIntegrationTest.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class RestTemplateIntegrationTest extends IntegrationTestBase {
    @Autowired protected TestRestTemplate restTemplate;
    @LocalServerPort protected int port;
    @Autowired protected JdbcTemplate jdbcTemplate;
}
```

- [ ] **Step 5: Verify context starts**

Run: `mvn test -Dtest=PingIntegrationTest`
Expected: Test class not found (no test methods yet), but Spring context should start without errors.

- [ ] **Step 6: Commit**

```bash
git add src/test/resources/application-test.yml src/test/java/io/github/lystrosaurus/atlasmountain/integration/
git commit -m "test: add integration test infrastructure"
```

---

### Task 2: PingIntegrationTest

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/PingIntegrationTest.java`

- [ ] **Step 1: Write `PingIntegrationTest.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PingIntegrationTest extends MockMvcIntegrationTest {

    @AfterEach
    void cleanup() {
        StpUtil.logout();
        jdbcTemplate.update("DELETE FROM api_token WHERE id = 9991");
    }

    @Test
    void publicPingReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void openPingWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/open/ping"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("COMMON_401"));
    }

    @Test
    void openPingWithValidTokenReturnsSuccess() throws Exception {
        String token = "ak_integration_ping";
        String hash = sha256(token);
        jdbcTemplate.update(
            "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at, created_at, created_by, updated_at, updated_by, deleted) " +
            "VALUES (9991, 'Ping Test', 'integration_ping', ?, 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
            hash, LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(get("/api/open/ping")
                .header("X-API-Token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"));
    }

    @Test
    void appPingWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/app/ping"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("COMMON_401"));
    }

    @Test
    void appPingWithSessionReturnsSuccess() throws Exception {
        StpUtil.login(1L);
        String token = StpUtil.getTokenValue();

        mockMvc.perform(get("/api/app/ping")
                .header("satoken", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.message").value("success"));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: Run PingIntegrationTest**

Run: `mvn test -Dtest=PingIntegrationTest`
Expected: 5 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/integration/PingIntegrationTest.java
git commit -m "test: add ping endpoint integration tests"
```

---

### Task 3: AuthIntegrationTest

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/AuthIntegrationTest.java`

- [ ] **Step 1: Write `AuthIntegrationTest.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.auth.vo.LoginVo;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends RestTemplateIntegrationTest {

    @Test
    void loginWithValidCredentialsReturnsToken() {
        ResponseEntity<ApiResponse<LoginVo>> response = restTemplate.exchange(
            "/api/auth/login",
            HttpMethod.POST,
            new HttpEntity<>(new LoginRequest("admin", "atlas-local")),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("0");
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorized() {
        ResponseEntity<ApiResponse<LoginVo>> response = restTemplate.exchange(
            "/api/auth/login",
            HttpMethod.POST,
            new HttpEntity<>(new LoginRequest("admin", "wrong-password")),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void sessionCookiePropagatesToProtectedEndpoint() {
        ResponseEntity<ApiResponse<LoginVo>> loginResponse = restTemplate.exchange(
            "/api/auth/login",
            HttpMethod.POST,
            new HttpEntity<>(new LoginRequest("admin", "atlas-local")),
            new ParameterizedTypeReference<>() {}
        );
        String cookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<ApiResponse<String>> pingResponse = restTemplate.exchange(
            "/api/app/ping",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(pingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pingResponse.getBody().getCode()).isEqualTo("0");
    }

    @Test
    void protectedEndpointWithoutCookieReturnsUnauthorized() {
        ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
            "/api/app/ping",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

- [ ] **Step 2: Run AuthIntegrationTest**

Run: `mvn test -Dtest=AuthIntegrationTest`
Expected: 4 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/integration/AuthIntegrationTest.java
git commit -m "test: add auth integration tests with TestRestTemplate"
```

---

### Task 4: ApiTokenIntegrationTest

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/ApiTokenIntegrationTest.java`

- [ ] **Step 1: Write `ApiTokenIntegrationTest.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiTokenIntegrationTest extends MockMvcIntegrationTest {

    @Test
    void malformedTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/open/ping")
                .header("X-API-Token", "invalid"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("COMMON_401"));
    }

    @Test
    void prefixNotFoundReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/open/ping")
                .header("X-API-Token", "ak_notexist_secret"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("COMMON_401"));
    }

    @Test
    void hashMismatchReturnsUnauthorized() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at, created_at, created_by, updated_at, updated_by, deleted) " +
            "VALUES (9992, 'Mismatch Test', 'mismatch', 'wronghash000000000000000000000000000000000000000000000000000000000000', 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
            LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(get("/api/open/ping")
                .header("X-API-Token", "ak_mismatch_secret"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("COMMON_401"));

        jdbcTemplate.update("DELETE FROM api_token WHERE id = 9992");
    }

    @Test
    void expiredTokenReturnsUnauthorized() throws Exception {
        String token = "ak_expired_test";
        String hash = sha256(token);
        jdbcTemplate.update(
            "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at, created_at, created_by, updated_at, updated_by, deleted) " +
            "VALUES (9993, 'Expired Test', 'expired', ?, 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
            hash, LocalDateTime.now().minusDays(1)
        );

        mockMvc.perform(get("/api/open/ping")
                .header("X-API-Token", token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("COMMON_401"));

        jdbcTemplate.update("DELETE FROM api_token WHERE id = 9993");
    }

    @Test
    void validTokenReturnsSuccess() throws Exception {
        String token = "ak_valid_test";
        String hash = sha256(token);
        jdbcTemplate.update(
            "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at, created_at, created_by, updated_at, updated_by, deleted) " +
            "VALUES (9994, 'Valid Test', 'valid', ?, 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
            hash, LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(get("/api/open/ping")
                .header("X-API-Token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"));

        jdbcTemplate.update("DELETE FROM api_token WHERE id = 9994");
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: Run ApiTokenIntegrationTest**

Run: `mvn test -Dtest=ApiTokenIntegrationTest`
Expected: 5 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/integration/ApiTokenIntegrationTest.java
git commit -m "test: add API token integration tests"
```

---

### Task 5: UserIntegrationTest

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/UserIntegrationTest.java`

- [ ] **Step 1: Write `UserIntegrationTest.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserIntegrationTest extends MockMvcIntegrationTest {

    @AfterEach
    void cleanup() {
        StpUtil.logout();
    }

    @Test
    void getCurrentUserWithoutLoginReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("COMMON_401"));
    }

    @Test
    void getCurrentUserAfterLoginReturnsUserData() throws Exception {
        StpUtil.login(1L);
        String token = StpUtil.getTokenValue();

        mockMvc.perform(get("/api/user/me")
                .header("satoken", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.username").value("admin"));
    }
}
```

- [ ] **Step 2: Run UserIntegrationTest**

Run: `mvn test -Dtest=UserIntegrationTest`
Expected: 2 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/integration/UserIntegrationTest.java
git commit -m "test: add user endpoint integration tests"
```

---

### Task 6: DistributedLockIntegrationTest

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/integration/DistributedLockIntegrationTest.java`

- [ ] **Step 1: Write `DistributedLockIntegrationTest.java`**

```java
package io.github.lystrosaurus.atlasmountain.integration;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.infra.redis.DistributedLockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistributedLockIntegrationTest extends MockMvcIntegrationTest {

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private RedissonClient redissonClient;

    private final List<String> keysToClean = new ArrayList<>();

    @AfterEach
    void cleanup() {
        for (String key : keysToClean) {
            redissonClient.getBucket(key).delete();
        }
        keysToClean.clear();
    }

    @Test
    void singleThreadAcquiresLockSuccessfully() {
        keysToClean.add("test-resource");

        String result = lockService.execute("test-resource", 1, 5, TimeUnit.SECONDS, () -> "done");

        assertThat(result).isEqualTo("done");
    }

    @Test
    void concurrentAccessOneSucceedsOneFails() throws Exception {
        String key = "concurrent-resource";
        keysToClean.add(key);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        AtomicReference<String> successResult = new AtomicReference<>();
        AtomicReference<Exception> failException = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                startLatch.await();
                String result = lockService.execute(key, 1, 10, TimeUnit.SECONDS, () -> {
                    Thread.sleep(3000);
                    return "A-done";
                });
                successResult.set(result);
            } catch (Exception e) {
                // ignore
            } finally {
                endLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                Thread.sleep(100);
                lockService.execute(key, 1, 10, TimeUnit.SECONDS, () -> "B-done");
            } catch (Exception e) {
                failException.set(e);
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successResult.get()).isEqualTo("A-done");
        assertThat(failException.get())
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> {
                BusinessException be = (BusinessException) ex;
                assertThat(be.errorCode()).isEqualTo(CommonErrorCode.LOCK_BUSY);
            });
    }

    @Test
    void lockAutoReleasesAfterLeaseTime() throws Exception {
        String key = "auto-release-key";
        keysToClean.add(key);

        String result1 = lockService.execute(key, 1, 2, TimeUnit.SECONDS, () -> "first");
        Thread.sleep(3000);
        String result2 = lockService.execute(key, 1, 2, TimeUnit.SECONDS, () -> "second");

        assertThat(result1).isEqualTo("first");
        assertThat(result2).isEqualTo("second");
    }
}
```

- [ ] **Step 2: Run DistributedLockIntegrationTest**

Run: `mvn test -Dtest=DistributedLockIntegrationTest`
Expected: 3 tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/integration/DistributedLockIntegrationTest.java
git commit -m "test: add distributed lock integration tests"
```

---

### Task 7: Final Verification and Documentation

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Run all integration tests**

Run: `mvn test -Dtest="*IntegrationTest"`
Expected: 19 tests PASS (5 + 4 + 5 + 2 + 3)

- [ ] **Step 2: Run full test suite**

Run: `mvn test`
Expected: All existing unit tests + architecture tests + integration tests PASS

- [ ] **Step 3: Update `CLAUDE.md`**

Add a new section under `## Testing Strategy`:

```markdown
- **Integration tests**: `@SpringBootTest` with MockMvc (most endpoints) and TestRestTemplate (auth session flow). Require local MySQL (`atlas_mountain_test` database) and Redis. Run with `mvn test -Dtest="*IntegrationTest"`.
```

- [ ] **Step 4: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: document integration tests in CLAUDE.md"
```

---

## Plan Self-Review

**Spec coverage:**
- [x] `application-test.yml` — Task 1, Step 1
- [x] Base classes with `@DirtiesContext` — Task 1, Steps 2-4
- [x] MockMvc tests (Ping, ApiToken, User, DistributedLock) — Tasks 2, 4, 5, 6
- [x] TestRestTemplate test (Auth) — Task 3
- [x] Database isolation (`atlas_mountain_test`) — Task 1, Step 1 + Prerequisites
- [x] Sa-Token session cleanup — `@AfterEach` StpUtil.logout() in PingIntegrationTest, UserIntegrationTest
- [x] Redis key cleanup — `@AfterEach` in DistributedLockIntegrationTest
- [x] `@DirtiesContext(AFTER_CLASS)` — Task 1, Step 2
- [x] API token edge cases — Task 4 (malformed, prefix not found, hash mismatch, expired, valid)
- [x] Distributed lock scenarios — Task 6 (single thread, concurrent, auto-release)

**Placeholder scan:** No TBD, TODO, or incomplete sections. All code blocks contain complete, copy-pasteable code.

**Type consistency:**
- `sha256` helper method used consistently in PingIntegrationTest and ApiTokenIntegrationTest
- `StpUtil.login(1L)` + `header("satoken", token)` pattern used consistently for MockMvc auth
- `ParameterizedTypeReference` used consistently for TestRestTemplate generic responses

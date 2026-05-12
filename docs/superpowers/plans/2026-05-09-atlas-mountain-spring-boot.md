# Atlas Mountain Spring Boot Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first runnable Atlas Mountain Spring Boot 4 single-application backend foundation.

**Architecture:** The application is a Maven-based Spring Boot monolith using strict package boundaries. Feature code follows Controller -> Service -> DAO -> DAO Impl -> Mapper, with ArchUnit tests preventing layer bypasses. MySQL schema is managed by Flyway, Sa-Token owns login sessions, API tokens protect open endpoints, and Redisson provides distributed locks.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Maven, MySQL, MyBatis-Plus, Sa-Token, Redisson, Flyway, spring-security-crypto, ArchUnit, JUnit 5, Mockito.

---

## Reference Documents

- Spec: `docs/superpowers/specs/2026-05-09-atlas-mountain-spring-boot-design.md`
- Dependency decision output required before feature implementation: `docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md`

## Planned File Map

Core build and docs:

- Create: `pom.xml` - Maven build, dependency versions, plugins.
- Modify: `README.md` - local setup, profiles, seed account warning, verification commands.
- Create: `docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md` - verified dependency table and fallback decisions.

Application and config:

- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/AtlasMountainApplication.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/WebConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/SaTokenConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/MybatisPlusConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/RedissonConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/JacksonConfig.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`

Common and web:

- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/response/ApiResponse.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/exception/ErrorCode.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/exception/CommonErrorCode.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/exception/BusinessException.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/web/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/web/log/RequestLogFilter.java`

Persistence and schema:

- Create: `src/main/resources/db/migration/V1__init_schema.sql`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/persistence/BaseEntity.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/persistence/AuditMetaObjectHandler.java`

User feature:

- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/entity/UserEntity.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/mapper/UserMapper.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/dao/UserDao.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/dao/impl/UserDaoImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/service/UserService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/service/UserServiceImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/vo/CurrentUserVo.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/controller/UserController.java`

Ops:

- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/ops/package-info.java`

Auth feature:

- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/entity/ApiTokenEntity.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/mapper/ApiTokenMapper.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/dao/ApiTokenDao.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/dao/impl/ApiTokenDaoImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/dto/LoginRequest.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/vo/LoginVo.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/AuthService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/AuthServiceImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/ApiTokenService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/ApiTokenServiceImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/AuthController.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/OpenPingController.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/AppPingController.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/PublicPingController.java`

Redisson lock:

- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLock.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/RedissonDistributedLockService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockKeyResolver.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockAspect.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/LockDemoController.java`

Tests:

- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/DependencyDecisionTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/common/response/ApiResponseTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/web/exception/GlobalExceptionHandlerTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/auth/service/ApiTokenServiceImplTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/auth/service/AuthServiceImplTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockKeyResolverTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/infra/redis/RedissonDistributedLockServiceTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/architecture/LayerArchitectureTest.java`

---

### Task 1: Dependency Compatibility Spike

**Files:**
- Create: `docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md`
- Create: `pom.xml`
- Test: `src/test/java/io/github/lystrosaurus/atlasmountain/DependencyDecisionTest.java`

- [ ] **Step 1: Verify current dependency versions**

Use official artifact metadata or Maven Central for these artifacts:

```text
org.springframework.boot:spring-boot-starter-parent
com.baomidou:mybatis-plus-spring-boot3-starter
cn.dev33:sa-token-spring-boot3-starter
org.redisson:redisson-spring-boot-starter
org.flywaydb:flyway-mysql
org.springframework.security:spring-security-crypto
com.tngtech.archunit:archunit-junit5
```

Record the exact versions selected in the dependency decision document. If a Boot 4 compatible starter is not available for MyBatis-Plus, Sa-Token, or Redisson, follow the fallback rules from the spec before proceeding.

- [ ] **Step 2: Write the dependency decision document**

Create `docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md` with this structure:

```markdown
# Atlas Mountain Dependency Decisions

## Compatibility Target

- Java: 21
- Spring Boot: 4.0.6
- Spring Framework generation: 7

## Verified Dependencies

| Capability | Artifact | Version | Decision | Notes |
| --- | --- | --- | --- | --- |
| Spring Boot | `org.springframework.boot:spring-boot-starter-parent` | `4.0.6` | Candidate | Stable Spring Boot 4 baseline. |
| MyBatis-Plus | `com.baomidou:mybatis-plus-spring-boot3-starter` | `3.5.16` | Candidate | Verify with Spring Boot 4 before feature work. |
| Sa-Token | `cn.dev33:sa-token-spring-boot3-starter` | `1.45.0` | Candidate | Verify with Spring Boot 4 before feature work. |
| Redisson | `org.redisson:redisson-spring-boot-starter` | `4.3.1` | Candidate | Verify with Spring Boot 4 before feature work. |
| Flyway MySQL | `org.flywaydb:flyway-mysql` | `12.5.0` | Candidate | Verify with Boot dependency management and MySQL. |
| BCrypt | `org.springframework.security:spring-security-crypto` | `7.0.5` | Candidate | Crypto only, no Spring Security web stack. |
| ArchUnit | `com.tngtech.archunit:archunit-junit5` | `1.4.2` | Candidate | Layer rule tests. |

## Fallback Decisions

- MyBatis-Plus fallback: if `mybatis-plus-spring-boot3-starter` is incompatible with Boot 4, switch to `mybatis-plus` core artifact plus manual `MybatisPlusConfig` (pagination interceptor, `SqlSessionFactory`); pause and ask before replacing with plain MyBatis or Spring Data JDBC.
- Sa-Token fallback: if `sa-token-spring-boot3-starter` is incompatible with Boot 4, switch to `sa-token-core` plus manual servlet filter / interceptor integration; pause and ask before replacing with Spring Security.
- Redisson fallback: if `redisson-spring-boot-starter` is incompatible with Boot 4, remove the starter and use `org.redisson:redisson` core with a manually defined `RedissonClient` bean in `config.RedissonConfig`.
```

Change `Decision` from `Candidate` to `Use` only after the local Maven verification in this task passes. If verification fails, record the fallback decision in the same table.

- [ ] **Step 3: Create the initial Maven build**

Create `pom.xml` using the verified versions:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/>
    </parent>

    <groupId>io.github.lystrosaurus</groupId>
    <artifactId>atlas-mountain</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>atlas-mountain</name>
    <description>Spring Boot foundation application for reusable backend capabilities.</description>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.16</mybatis-plus.version>
        <sa-token.version>1.45.0</sa-token.version>
        <redisson.version>4.3.1</redisson.version>
        <archunit.version>1.4.2</archunit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot3-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>${redisson.version}</version>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-crypto</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit-junit5</artifactId>
            <version>${archunit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

If Maven verification fails, update the affected property according to the dependency decision document and rerun `mvn test`.

- [ ] **Step 4: Write a dependency decision guard test**

Create `src/test/java/io/github/lystrosaurus/atlasmountain/DependencyDecisionTest.java`:

```java
package io.github.lystrosaurus.atlasmountain;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyDecisionTest {

    @Test
    void dependencyDecisionDocumentDoesNotContainPlaceholders() throws Exception {
        String content = Files.readString(Path.of("docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md"));

        assertThat(content)
                .doesNotContain("Candidate")
                .doesNotContain("unverified");
    }
}
```

- [ ] **Step 5: Run build verification**

Run:

```bash
mvn compile
```

Expected: Maven resolves all dependencies and compilation succeeds. If dependency resolution fails due to Boot 4 compatibility, update the decision document and `pom.xml` using the fallback rules before continuing.

- [ ] **Step 6: Commit**

```bash
git add pom.xml docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md src/test/java/io/github/lystrosaurus/atlasmountain/DependencyDecisionTest.java
git commit -m "chore: initialize spring boot dependency baseline"
```

---

### Task 2: Application Skeleton And Configuration

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/AtlasMountainApplication.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/WebConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/JacksonConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/ops/package-info.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/web/log/RequestLogFilter.java`
- Modify: `README.md`

- [ ] **Step 1: Create the application entry point**

Create `AtlasMountainApplication.java`:

```java
package io.github.lystrosaurus.atlasmountain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AtlasMountainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasMountainApplication.class, args);
    }
}
```

- [ ] **Step 2: Add request log filter**

Create `RequestLogFilter.java`:

```java
package io.github.lystrosaurus.atlasmountain.web.log;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class RequestLogFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} {} {} - {}ms",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    wrappedResponse.getStatus(),
                    duration);
            wrappedResponse.copyBodyToResponse();
        }
    }
}
```

- [ ] **Step 3: Create basic config classes**

Create `WebConfig.java`:

```java
package io.github.lystrosaurus.atlasmountain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
}
```

Create `JacksonConfig.java`:

```java
package io.github.lystrosaurus.atlasmountain.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer atlasJacksonCustomizer() {
        return builder -> builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```

- [ ] **Step 4: Create ops package placeholder**

Create `src/main/java/io/github/lystrosaurus/atlasmountain/ops/package-info.java`:

```java
package io.github.lystrosaurus.atlasmountain.ops;
```

This package holds operational endpoints and support classes that are not core business features. It is intentionally empty in the first version.

- [ ] **Step 5: Create application configuration**

Create `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: atlas-mountain
  profiles:
    active: local
  jackson:
    default-property-inclusion: non_null

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

Create `src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/atlas_mountain?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: atlas
    password: atlas
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration

  redis:
    host: localhost
    port: 6379
```

- [ ] **Step 5: Create .gitignore for local profiles**

Create `.gitignore`:

```text
application-local.yml
```

`application-local.yml` contains developer-specific database credentials and must not be committed.

- [ ] **Step 6: Update README local setup**

Replace `README.md` with:

````markdown
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
````

- [ ] **Step 6: Run build verification**

Run:

```bash
mvn compile
```

Expected: PASS. The app may not start without MySQL/Redis yet; this task verifies compilation only.

- [ ] **Step 7: Commit**

```bash
git add README.md src/main/java/io/github/lystrosaurus/atlasmountain src/main/resources/application.yml .gitignore
git commit -m "feat: add spring boot application skeleton"
```

---

### Task 3: Common Response And Exception Handling

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/response/ApiResponse.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/exception/ErrorCode.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/exception/CommonErrorCode.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/common/exception/BusinessException.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/web/exception/GlobalExceptionHandler.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/common/response/ApiResponseTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/web/exception/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write response tests**

Create `ApiResponseTest.java`:

```java
package io.github.lystrosaurus.atlasmountain.common.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successWrapsDataWithSuccessCode() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.code()).isEqualTo("0");
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ok");
    }

    @Test
    void failureWrapsErrorCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.failure("AUTH_401", "login required");

        assertThat(response.code()).isEqualTo("AUTH_401");
        assertThat(response.message()).isEqualTo("login required");
        assertThat(response.data()).isNull();
    }
}
```

- [ ] **Step 2: Implement response and exception primitives**

Create `ApiResponse.java`:

```java
package io.github.lystrosaurus.atlasmountain.common.response;

public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("0", "success", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("0", "success", null);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

Create `ErrorCode.java`:

```java
package io.github.lystrosaurus.atlasmountain.common.exception;

public interface ErrorCode {

    String code();

    String message();
}
```

Create `CommonErrorCode.java`:

```java
package io.github.lystrosaurus.atlasmountain.common.exception;

public enum CommonErrorCode implements ErrorCode {
    BAD_REQUEST("COMMON_400", "bad request"),
    UNAUTHORIZED("COMMON_401", "unauthorized"),
    FORBIDDEN("COMMON_403", "forbidden"),
    NOT_FOUND("COMMON_404", "not found"),
    CONFLICT("COMMON_409", "conflict"),
    INTERNAL_ERROR("COMMON_500", "internal server error"),
    LOCK_BUSY("LOCK_409", "resource is busy");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
```

Create `BusinessException.java`:

```java
package io.github.lystrosaurus.atlasmountain.common.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
```

- [ ] **Step 3: Write exception handler test**

Create `GlobalExceptionHandlerTest.java`:

```java
package io.github.lystrosaurus.atlasmountain.web.exception;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void businessExceptionMapsToErrorResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(
                new BusinessException(CommonErrorCode.UNAUTHORIZED)
        );

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("COMMON_401");
    }
}
```

- [ ] **Step 4: Implement global exception handler**

Create `GlobalExceptionHandler.java`:

```java
package io.github.lystrosaurus.atlasmountain.web.exception;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        HttpStatus status = switch (exception.errorCode().code()) {
            case "COMMON_401" -> HttpStatus.UNAUTHORIZED;
            case "COMMON_403" -> HttpStatus.FORBIDDEN;
            case "COMMON_404" -> HttpStatus.NOT_FOUND;
            case "COMMON_409", "LOCK_409" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(exception.errorCode().code(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(CommonErrorCode.BAD_REQUEST.code(), "validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(CommonErrorCode.INTERNAL_ERROR.code(), CommonErrorCode.INTERNAL_ERROR.message()));
    }
}
```

- [ ] **Step 6: Run tests**

Run:

```bash
mvn test -Dtest=ApiResponseTest,GlobalExceptionHandlerTest
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/common src/main/java/io/github/lystrosaurus/atlasmountain/web src/test/java/io/github/lystrosaurus/atlasmountain/common src/test/java/io/github/lystrosaurus/atlasmountain/web
git commit -m "feat: add common response and exception handling"
```

---

### Task 4: Persistence Schema And MyBatis-Plus Foundation

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/MybatisPlusConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/persistence/BaseEntity.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/persistence/AuditMetaObjectHandler.java`
- Create: `src/main/resources/db/migration/V1__init_schema.sql`

- [ ] **Step 1: Add MyBatis-Plus configuration**

Create `MybatisPlusConfig.java`:

```java
package io.github.lystrosaurus.atlasmountain.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("io.github.lystrosaurus.atlasmountain.**.mapper")
public class MybatisPlusConfig {

    @Bean
    MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

- [ ] **Step 2: Add base entity and audit handler**

Create `BaseEntity.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.persistence;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.time.LocalDateTime;

public abstract class BaseEntity {

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableLogic
    private Boolean deleted;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
```

Create `AuditMetaObjectHandler.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.persistence;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final Long SYSTEM_USER_ID = 0L;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createdBy", Long.class, SYSTEM_USER_ID);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedBy", Long.class, SYSTEM_USER_ID);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, "updatedBy", Long.class, SYSTEM_USER_ID);
    }
}
```

- [ ] **Step 3: Create Flyway schema**

Create `V1__init_schema.sql`:

```sql
CREATE TABLE sys_user (
    id BIGINT NOT NULL PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by BIGINT NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by BIGINT NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE TABLE api_token (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    token_prefix VARCHAR(32) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    expires_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    created_by BIGINT NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by BIGINT NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uk_api_token_prefix UNIQUE (token_prefix),
    CONSTRAINT uk_api_token_hash UNIQUE (token_hash)
);

INSERT INTO sys_user (
    id, username, password_hash, nickname, status,
    created_at, created_by, updated_at, updated_by, deleted
) VALUES (
    1,
    'admin',
    '$2a$10$9Wigb53vq0SUqjkfGVDGXe6af8QTwslO5M7NYdjfVv.aARqLio2ea',
    'Local Admin',
    'ENABLED',
    NOW(),
    0,
    NOW(),
    0,
    0
);
```

The seed password hash must be documented in README as local-only. If the hash does not match the README password during implementation, regenerate it with `BCryptPasswordEncoder`.

- [ ] **Step 4: Update README seed account**

Add this under `Development Seed Account`:

```markdown
Default local login:

- Username: `admin`
- Password: `atlas-local`

This credential is only for local verification. Do not use it in production.
```

- [ ] **Step 5: Run build verification**

Run:

```bash
mvn test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add README.md src/main/java/io/github/lystrosaurus/atlasmountain/config/MybatisPlusConfig.java src/main/java/io/github/lystrosaurus/atlasmountain/infra/persistence src/main/resources/db/migration/V1__init_schema.sql
git commit -m "feat: add persistence foundation and schema"
```

---

### Task 5: User DAO Layer

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/entity/UserEntity.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/mapper/UserMapper.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/dao/UserDao.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/dao/impl/UserDaoImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/service/UserService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/service/UserServiceImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/vo/CurrentUserVo.java`

- [ ] **Step 1: Add user entity and mapper**

Create `UserEntity.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.lystrosaurus.atlasmountain.infra.persistence.BaseEntity;

@TableName("sys_user")
public class UserEntity extends BaseEntity {

    @TableId
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

Create `UserMapper.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;

public interface UserMapper extends BaseMapper<UserEntity> {
}
```

- [ ] **Step 2: Add user DAO**

Create `UserDao.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.dao;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;

import java.util.Optional;

public interface UserDao {

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);
}
```

Create `UserDaoImpl.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.lystrosaurus.atlasmountain.user.dao.UserDao;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final UserMapper userMapper;

    public UserDaoImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .last("LIMIT 1");
        return Optional.ofNullable(userMapper.selectOne(wrapper));
    }
}
```

- [ ] **Step 3: Add user service and VO**

Create `CurrentUserVo.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.vo;

public record CurrentUserVo(Long id, String username, String nickname) {
}
```

Create `UserService.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.service;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;

import java.util.Optional;

public interface UserService {

    Optional<UserEntity> findLoginUser(String username);

    CurrentUserVo getCurrentUser(Long userId);
}
```

Create `UserServiceImpl.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.service;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.user.dao.UserDao;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<UserEntity> findLoginUser(String username) {
        return userDao.findByUsername(username)
                .filter(user -> "ENABLED".equals(user.getStatus()));
    }

    @Override
    public CurrentUserVo getCurrentUser(Long userId) {
        UserEntity user = userDao.findById(userId)
                .filter(candidate -> "ENABLED".equals(candidate.getStatus()))
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
        return new CurrentUserVo(user.getId(), user.getUsername(), user.getNickname());
    }
}
```

- [ ] **Step 4: Run build verification**

Run:

```bash
mvn test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/user
git commit -m "feat: add user dao layer"
```

---

### Task 6: Sa-Token Login And API Token Services

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/SaTokenConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/entity/ApiTokenEntity.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/mapper/ApiTokenMapper.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/dao/ApiTokenDao.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/dao/impl/ApiTokenDaoImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/dto/LoginRequest.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/vo/LoginVo.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/AuthService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/AuthServiceImpl.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/ApiTokenService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/service/ApiTokenServiceImpl.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/auth/service/ApiTokenServiceImplTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/auth/service/AuthServiceImplTest.java`

- [ ] **Step 1: Add auth model and DAO**

Create `ApiTokenEntity.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.lystrosaurus.atlasmountain.infra.persistence.BaseEntity;

import java.time.LocalDateTime;

@TableName("api_token")
public class ApiTokenEntity extends BaseEntity {

    @TableId
    private Long id;
    private String name;
    private String tokenPrefix;
    private String tokenHash;
    private String status;
    private LocalDateTime expiresAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTokenPrefix() { return tokenPrefix; }
    public void setTokenPrefix(String tokenPrefix) { this.tokenPrefix = tokenPrefix; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
```

Create `ApiTokenMapper.java`, `ApiTokenDao.java`, and `ApiTokenDaoImpl.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;

public interface ApiTokenMapper extends BaseMapper<ApiTokenEntity> {
}
```

```java
package io.github.lystrosaurus.atlasmountain.auth.dao;

import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;

import java.util.Optional;

public interface ApiTokenDao {

    Optional<ApiTokenEntity> findByPrefix(String tokenPrefix);
}
```

```java
package io.github.lystrosaurus.atlasmountain.auth.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.lystrosaurus.atlasmountain.auth.dao.ApiTokenDao;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;
import io.github.lystrosaurus.atlasmountain.auth.mapper.ApiTokenMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ApiTokenDaoImpl implements ApiTokenDao {

    private final ApiTokenMapper apiTokenMapper;

    public ApiTokenDaoImpl(ApiTokenMapper apiTokenMapper) {
        this.apiTokenMapper = apiTokenMapper;
    }

    @Override
    public Optional<ApiTokenEntity> findByPrefix(String tokenPrefix) {
        LambdaQueryWrapper<ApiTokenEntity> wrapper = new LambdaQueryWrapper<ApiTokenEntity>()
                .eq(ApiTokenEntity::getTokenPrefix, tokenPrefix)
                .last("LIMIT 1");
        return Optional.ofNullable(apiTokenMapper.selectOne(wrapper));
    }
}
```

- [ ] **Step 2: Write API token service tests**

Create `ApiTokenServiceImplTest.java` with mocked DAO:

```java
package io.github.lystrosaurus.atlasmountain.auth.service;

import io.github.lystrosaurus.atlasmountain.auth.dao.ApiTokenDao;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiTokenServiceImplTest {

    @Test
    void validTokenPasses() {
        ApiTokenDao dao = mock(ApiTokenDao.class);
        ApiTokenEntity entity = new ApiTokenEntity();
        entity.setTokenPrefix("demo");
        entity.setTokenHash("f2fb678a36003f9288eac1b68530881b5d6f745e5876a8ad9788442e156c2178");
        entity.setStatus("ENABLED");
        entity.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(dao.findByPrefix("demo")).thenReturn(Optional.of(entity));

        ApiTokenService service = new ApiTokenServiceImpl(dao);

        assertThatCode(() -> service.verify("ak_demo_secret")).doesNotThrowAnyException();
    }

    @Test
    void malformedTokenFails() {
        ApiTokenService service = new ApiTokenServiceImpl(mock(ApiTokenDao.class));

        assertThatThrownBy(() -> service.verify("invalid"))
                .isInstanceOf(BusinessException.class);
    }
}
```

- [ ] **Step 3: Implement API token service**

Create `ApiTokenService.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.service;

public interface ApiTokenService {

    void verify(String token);
}
```

Create `ApiTokenServiceImpl.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.service;

import io.github.lystrosaurus.atlasmountain.auth.dao.ApiTokenDao;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class ApiTokenServiceImpl implements ApiTokenService {

    private final ApiTokenDao apiTokenDao;

    public ApiTokenServiceImpl(ApiTokenDao apiTokenDao) {
        this.apiTokenDao = apiTokenDao;
    }

    @Override
    public void verify(String token) {
        String prefix = extractPrefix(token);
        ApiTokenEntity apiToken = apiTokenDao.findByPrefix(prefix)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
        if (!"ENABLED".equals(apiToken.getStatus())) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        if (apiToken.getExpiresAt() != null && apiToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        if (!sha256(token).equals(apiToken.getTokenHash())) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }

    private String extractPrefix(String token) {
        if (token == null || !token.startsWith("ak_")) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        String[] parts = token.split("_", 3);
        if (parts.length != 3 || parts[1].isBlank() || parts[2].isBlank()) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        return parts[1];
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
```

- [ ] **Step 4: Implement login service**

Create `LoginRequest.java`, `LoginVo.java`, `AuthService.java`, and `AuthServiceImpl.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
```

```java
package io.github.lystrosaurus.atlasmountain.auth.vo;

public record LoginVo(String tokenName, String tokenValue) {
}
```

```java
package io.github.lystrosaurus.atlasmountain.auth.service;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.auth.vo.LoginVo;

public interface AuthService {

    LoginVo login(LoginRequest request);

    void logout();
}
```

```java
package io.github.lystrosaurus.atlasmountain.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.auth.vo.LoginVo;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public LoginVo login(LoginRequest request) {
        UserEntity user = userService.findLoginUser(request.username())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        StpUtil.login(user.getId());
        return new LoginVo(StpUtil.getTokenName(), StpUtil.getTokenValue());
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
```

- [ ] **Step 5: Run tests**

Run:

```bash
mvn test -Dtest=ApiTokenServiceImplTest,AuthServiceImplTest
```

Expected: API token tests pass. `AuthServiceImplTest` tests password mismatch with a mocked `UserService` and asserts `BusinessException` is thrown before `StpUtil.login` is reached. Sa-Token static context is not exercised in unit tests.

Create `AuthServiceImplTest.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.service;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @Test
    void loginWithWrongPasswordThrowsUnauthorized() {
        UserService userService = mock(UserService.class);
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqhmM6JGKpS4G3R1G2JH8YpfB0Bqy");
        user.setStatus("ENABLED");
        when(userService.findLoginUser("admin")).thenReturn(Optional.of(user));

        AuthService authService = new AuthServiceImpl(userService);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("unauthorized");
    }

    @Test
    void loginWithUnknownUserThrowsUnauthorized() {
        UserService userService = mock(UserService.class);
        when(userService.findLoginUser("unknown")).thenReturn(Optional.empty());

        AuthService authService = new AuthServiceImpl(userService);

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "any-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("unauthorized");
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/auth src/test/java/io/github/lystrosaurus/atlasmountain/auth
git commit -m "feat: add lightweight auth services"
```

---

### Task 7: Auth Controllers And Endpoint Categories

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/config/SaTokenConfig.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/AuthController.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/PublicPingController.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/OpenPingController.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/AppPingController.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/user/controller/UserController.java`

- [ ] **Step 1: Configure Sa-Token route checks**

Create `SaTokenConfig.java`:

```java
package io.github.lystrosaurus.atlasmountain.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.auth.service.ApiTokenService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private final ApiTokenService apiTokenService;

    public SaTokenConfig(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/api/open/**", () -> apiTokenService.verify(SaHolder.getRequest().getHeader("X-API-Token")));
            SaRouter.match("/api/app/**", StpUtil::checkLogin);
        })).addPathPatterns("/**");
    }
}
```

- [ ] **Step 2: Add controllers**

Create `AuthController.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.controller;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.auth.service.AuthService;
import io.github.lystrosaurus.atlasmountain.auth.vo.LoginVo;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginVo> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }
}
```

Create ping controllers:

```java
package io.github.lystrosaurus.atlasmountain.auth.controller;

import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicPingController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("public-pong");
    }
}
```

```java
package io.github.lystrosaurus.atlasmountain.auth.controller;

import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open")
public class OpenPingController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("open-pong");
    }
}
```

```java
package io.github.lystrosaurus.atlasmountain.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class AppPingController {

    private final UserService userService;

    public AppPingController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("app-pong");
    }
}
```

Create `UserController.java`:

```java
package io.github.lystrosaurus.atlasmountain.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> me() {
        return ApiResponse.success(userService.getCurrentUser(StpUtil.getLoginIdAsLong()));
    }
}
```

- [ ] **Step 3: Run build verification**

Run:

```bash
mvn test
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/config/SaTokenConfig.java src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller src/main/java/io/github/lystrosaurus/atlasmountain/user/controller
git commit -m "feat: add authenticated endpoint categories"
```

---

### Task 8: Redisson Distributed Lock Capability

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLock.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/RedissonDistributedLockService.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockKeyResolver.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockAspect.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller/LockDemoController.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/infra/redis/DistributedLockKeyResolverTest.java`
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/infra/redis/RedissonDistributedLockServiceTest.java`

- [ ] **Step 1: Verify Redisson starter auto-configuration**

No manual `RedissonConfig` is required. The `redisson-spring-boot-starter` (verified in Task 1) auto-configures a `RedissonClient` bean from `spring.redis.*` properties defined in `application-local.yml`.

- [ ] **Step 2: Write key resolver test"

Create `DistributedLockKeyResolverTest.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.redis;

import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static org.assertj.core.api.Assertions.assertThat;

class DistributedLockKeyResolverTest {

    @Test
    void resolvesTemplateWithSpelVariable() {
        DistributedLockKeyResolver resolver = new DistributedLockKeyResolver();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("resourceId", "42");

        String key = resolver.resolve("lock:#{#resourceId}", context);

        assertThat(key).isEqualTo("lock:42");
    }
}
```

- [ ] **Step 3: Implement lock types**

Create `DistributedLock.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();

    long waitTime() default 1;

    long leaseTime() default 10;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
```

Create `DistributedLockService.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.redis;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface DistributedLockService {

    <T> T execute(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Callable<T> action);
}
```

Create `DistributedLockKeyResolver.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.redis;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class DistributedLockKeyResolver {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParserContext parserContext = new TemplateParserContext();

    public String resolve(String expression, StandardEvaluationContext context) {
        return parser.parseExpression(expression, parserContext).getValue(context, String.class);
    }
}
```

- [ ] **Step 4: Implement Redisson lock service**

Create `RedissonDistributedLockService.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.redis;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
public class RedissonDistributedLockService implements DistributedLockService {

    private final RedissonClient redissonClient;

    public RedissonDistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> T execute(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Callable<T> action) {
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!locked) {
                throw new BusinessException(CommonErrorCode.LOCK_BUSY);
            }
            return action.call();
        } catch (BusinessException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CommonErrorCode.LOCK_BUSY);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

- [ ] **Step 5: Implement aspect and demo controller**

Create `DistributedLockAspect.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DistributedLockAspect {

    private final DistributedLockService distributedLockService;
    private final DistributedLockKeyResolver keyResolver;

    public DistributedLockAspect(DistributedLockService distributedLockService, DistributedLockKeyResolver keyResolver) {
        this.distributedLockService = distributedLockService;
        this.keyResolver = keyResolver;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        String key = keyResolver.resolve(distributedLock.key(), context);
        return distributedLockService.execute(key, distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit(), joinPoint::proceed);
    }
}
```

Create `LockDemoController.java`:

```java
package io.github.lystrosaurus.atlasmountain.auth.controller;

import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/locks/demo")
public class LockDemoController {

    @PostMapping("/{resourceId}")
    @DistributedLock(key = "demo:#{#resourceId}", waitTime = 1, leaseTime = 5)
    public ApiResponse<String> demo(@PathVariable String resourceId) {
        return ApiResponse.success("locked:" + resourceId);
    }
}
```

- [ ] **Step 6: Write Redisson lock service test**

Create `RedissonDistributedLockServiceTest.java`:

```java
package io.github.lystrosaurus.atlasmountain.infra.redis;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RedissonDistributedLockServiceTest {

    @Test
    void executesActionWhenLockAcquired() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("test-key")).thenReturn(lock);
        when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenReturn(true);

        RedissonDistributedLockService service = new RedissonDistributedLockService(redissonClient);
        String result = service.execute("test-key", 1, 5, TimeUnit.SECONDS, () -> "done");

        assertThat(result).isEqualTo("done");
        verify(lock).unlock();
    }

    @Test
    void throwsBusinessExceptionWhenLockNotAcquired() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("busy-key")).thenReturn(lock);
        when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenReturn(false);

        RedissonDistributedLockService service = new RedissonDistributedLockService(redissonClient);

        assertThatThrownBy(() -> service.execute("busy-key", 1, 5, TimeUnit.SECONDS, () -> "done"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).errorCode()).isEqualTo(CommonErrorCode.LOCK_BUSY));
    }

    @Test
    void throwsBusinessExceptionWhenInterrupted() throws Exception {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("interrupt-key")).thenReturn(lock);
        when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        RedissonDistributedLockService service = new RedissonDistributedLockService(redissonClient);

        assertThatThrownBy(() -> service.execute("interrupt-key", 1, 5, TimeUnit.SECONDS, () -> "done"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).errorCode()).isEqualTo(CommonErrorCode.LOCK_BUSY));
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }
}
```

- [ ] **Step 7: Run tests**

Run:

```bash
mvn test -Dtest=DistributedLockKeyResolverTest,RedissonDistributedLockServiceTest
```

Expected: PASS. `RedissonDistributedLockServiceTest` uses Mockito and does not require a running Redis instance.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/infra/redis src/main/java/io/github/lystrosaurus/atlasmountain/auth/controller src/test/java/io/github/lystrosaurus/atlasmountain/infra/redis
git commit -m "feat: add redisson distributed lock support"
```

---

### Task 9: Architecture Guardrails

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/architecture/LayerArchitectureTest.java`

- [ ] **Step 1: Add ArchUnit test**

Create `LayerArchitectureTest.java`:

```java
package io.github.lystrosaurus.atlasmountain.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "io.github.lystrosaurus.atlasmountain",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class LayerArchitectureTest {

    @ArchTest
    static final ArchRule controllers_do_not_depend_on_dao_mapper_or_entity =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAnyPackage("..dao..", "..mapper..", "..entity..");

    @ArchTest
    static final ArchRule services_do_not_depend_on_mappers =
            noClasses().that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..mapper..");

    @ArchTest
    static final ArchRule only_dao_impl_depends_on_mappers =
            noClasses().that().resideOutsideOfPackage("..dao.impl..")
                    .should().dependOnClassesThat().resideInAPackage("..mapper..");

    @ArchTest
    static final ArchRule controllers_do_not_depend_on_entity =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..entity..");
}
```

- [ ] **Step 2: Run architecture test**

Run:

```bash
mvn test -Dtest=LayerArchitectureTest
```

Expected: PASS. If it fails, fix the dependency direction rather than weakening the rule.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/architecture/LayerArchitectureTest.java
git commit -m "test: enforce layer architecture"
```

---

### Task 10: Final Verification And Documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Update README verification commands**

Add:

````markdown
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
````

- [ ] **Step 2: Run full test suite**

Run:

```bash
mvn test
```

Expected: PASS.

- [ ] **Step 3: Run local startup when MySQL and Redis are configured**

Run:

```bash
mvn spring-boot:run
```

Expected: application starts on port `8080`, Flyway applies `V1__init_schema.sql`, and Actuator health is available.

- [ ] **Step 4: Commit**

```bash
git add README.md
git commit -m "docs: document local verification"
```

---

## Self-Review

Spec coverage:

- Spring Boot 4, Java 21, Maven single application: Task 1 and Task 2.
- Dependency compatibility spike and decision document: Task 1.
- Package structure and config package: Task 2 and later feature tasks.
- Strict Controller -> Service -> DAO -> DAO Impl -> Mapper layering: Task 5, Task 6, Task 7, Task 9.
- Unified response and exception handling: Task 3.
- MySQL, MyBatis-Plus, Flyway, audit fields: Task 4.
- Sa-Token login and API token categories: Task 6 and Task 7.
- API token prefix/hash strategy: Task 6.
- BCrypt through `spring-security-crypto`: Task 6.
- Redisson distributed lock service and annotation: Task 8.
- No Testcontainers: all tests use unit, MVC, mock, or ArchUnit style only.
- README seed account warning and local verification: Task 2, Task 4, Task 10.
- `spring-boot-starter-validation` dependency: Task 1 (pom.xml).
- `ops` package placeholder: Task 2.

Placeholder scan:

- The dependency decision task starts from concrete candidate versions and requires replacing `Candidate` decisions with `Use` or fallback decisions after Maven verification.
- Implementation tasks use concrete file paths and commands.

Type consistency:

- `ApiResponse`, `BusinessException`, `CommonErrorCode`, `UserService`, `ApiTokenService`, and Redisson lock types are referenced consistently across tasks.

# Atlas Mountain Dependency Decisions

## Compatibility Target

- Java: 21
- Spring Boot: 4.0.6
- Spring Framework generation: 7

## Verified Dependencies

| Capability | Artifact | Version | Decision | Notes |
| --- | --- | --- | --- | --- |
| Spring Boot | `org.springframework.boot:spring-boot-starter-parent` | `4.0.6` | Use | Stable Spring Boot 4 baseline. |
| MyBatis-Plus | `com.baomidou:mybatis-plus-spring-boot3-starter` | `3.5.16` | Use | Verified with Spring Boot 4.0.6; compilation passes. |
| Sa-Token | `cn.dev33:sa-token-spring-boot3-starter` | `1.45.0` | Use | Verified with Spring Boot 4.0.6; compilation passes. |
| Redisson | `org.redisson:redisson-spring-boot-starter` | `4.3.1` | Use | Verified with Spring Boot 4.0.6; compilation passes. |
| Flyway MySQL | `org.flywaydb:flyway-mysql` | `12.5.0` | Use | Verified with Boot 4.0.6 dependency management and MySQL connector. |
| BCrypt | `org.springframework.security:spring-security-crypto` | `7.0.5` | Use | Crypto only, no Spring Security web stack. |
| ArchUnit | `com.tngtech.archunit:archunit-junit5` | `1.4.2` | Use | Layer rule tests. |

## Fallback Decisions

- MyBatis-Plus fallback: if `mybatis-plus-spring-boot3-starter` is incompatible with Boot 4, switch to `mybatis-plus` core artifact plus manual `MybatisPlusConfig` (pagination interceptor, `SqlSessionFactory`); pause and ask before replacing with plain MyBatis or Spring Data JDBC.
- Sa-Token fallback: if `sa-token-spring-boot3-starter` is incompatible with Boot 4, switch to `sa-token-core` plus manual servlet filter / interceptor integration; pause and ask before replacing with Spring Security.
- Redisson fallback: if `redisson-spring-boot-starter` is incompatible with Boot 4, remove the starter and use `org.redisson:redisson` core with a manually defined `RedissonClient` bean in `config.RedissonConfig`.

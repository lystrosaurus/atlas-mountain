# Changelog

本项目的重要变更记录。格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [Unreleased]

### Changed

- **审计字段接入真实用户**：`AuditMetaObjectHandler` 从硬编码 `0L` 改为读取 `UserContext` ThreadLocal；新增 `infra.context.UserContext` 与 `web.UserContextInterceptor`（web 层在 Sa-Token 认证后注入登录用户 ID，DAO 层不再依赖 Sa-Token）。
- **异常映射重构**：`ErrorCode` 接口新增 `httpStatus()` 方法，`CommonErrorCode` 显式声明每个错误码的 HTTP 状态码；`GlobalExceptionHandler` 删除脆弱的字符串 switch，改为 `HttpStatus.valueOf(errorCode.httpStatus())`。
- **Sa-Token 异常覆盖**：`GlobalExceptionHandler` 新增 `NotRoleException` / `NotPermissionException` / `DisableServiceException` 处理，统一映射到 403 FORBIDDEN（原先落到兜底 handler 返回 500）。
- **CDC 异常日志增强**：`BinlogEventDispatcher` 的 `onEvent` 与 `safeHandle` 异常捕获补充 `eventType` / `handler` / `database` / `table` 上下文，便于定位失败事件；`safeHandle` 改签名以支持 handler 隔离测试。
- **controller 模块归属调整**：`AppPingController` / `OpenPingController` / `PublicPingController` / `LockDemoController` 从 `auth.controller` 移至新建的 `system.controller` 包（`auth.controller` 仅保留 `AuthController`）。
- **CDC 示例配置对齐**：`dev/application-local.yml.example` 的 `max-retries` 从 `10` 改为 `-1`，与 `CdcProperties` 代码默认值（无限重试）一致，并加注释说明。

### Fixed

- **LRU 容量 off-by-one**：`BinlogEventDispatcher` 的 `tableMap` 驱逐条件从 `size() > MAX` 改为 `size() >= MAX`，实际容量回归 1000。
- **移除冗余 `LIMIT 1`**：`ApiTokenDaoImpl.findByPrefix` 与 `UserDaoImpl.findByUsername` 移除多余的 `LIMIT 1`（`token_prefix` / `username` 均有 UNIQUE 约束，`selectOne` 已保证唯一）。

### Security

- **测试配置凭据改 env 占位**：`application-test.yml` 的 `username` / `password` 从硬编码 `root` 改为 `${MYSQL_USER:root}` / `${MYSQL_PASSWORD:root}`，CI 可注入不同凭据。

### Added

- **JaCoCo 覆盖率插件**：`pom.xml` 新增 `jacoco-maven-plugin:0.8.12`，`mvn test` 后生成报告至 `target/site/jacoco/`。

### Docs

- **同步代码结构文档**：`AGENTS.md` 与 `CLAUDE.md` 的代码结构部分补上 `system` 模块（P2-4 新增）及 `infra.context` / `web.UserContextInterceptor`（P0-1 新增）。

## [0.0.1-SNAPSHOT] - 初始版本

Spring Boot 4 单体后端模板，Java 21，含 Sa-Token 会话 + API Token 双认证、登录与当前用户查询、注解限流、Redisson 分布式锁、MyBatis-Plus + Flyway、MySQL binlog CDC。

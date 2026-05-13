# Atlas Mountain 编码规范

> 本规范适用于 Atlas Mountain 项目的所有 Java 源代码、SQL 迁移脚本和配置文件。

---

## 1. 代码风格与格式化

### 1.1 基础规则

| 规则 | 值 | 说明 |
|------|-----|------|
| 缩进 | 2 空格 | 禁止使用 Tab |
| 换行符 | LF (`\n`) | 统一 Unix 风格换行 |
| 编码 | UTF-8 | 无 BOM |
| 行尾空格 | 去除 | 保存时自动删除 |
| 文件末尾 | 保留空行 | 每个文件末尾保留一个空行 |
| 最大行宽 | 120 字符 | 超过时合理换行 |

### 1.2 Java 格式

- **大括号**：K&R 风格（左大括号不换行）
- **空格**：`if`、`for`、`while`、`switch` 后加空格；运算符两侧加空格
- **空行**：package 与 import 之间、import 与类声明之间各一个空行
- **导入**：每个类单独 import，禁止 `import *.*`
- **废弃 API**：禁止使用 `@Deprecated` 标注的 API；编译期若产生 deprecation 警告必须消除
- **注解**：方法/类注解每个占一行；参数注解与参数同行

```java
// 正确
@GetMapping("/me")
public ApiResponse<CurrentUserVo> me() {
    return ApiResponse.success(userService.getCurrentUser(StpUtil.getLoginIdAsLong()));
}

// 错误 - 左大括号换行
public ApiResponse<CurrentUserVo> me()
{
    ...
}
```

### 1.3 格式化工具

项目使用 **Spotless** Maven 插件强制执行格式。提交前运行：

```bash
# 检查格式
mvn spotless:check

# 自动修复格式
mvn spotless:apply
```

---

## 2. 命名约定

### 2.1 包命名

```
io.github.lystrosaurus.atlasmountain
├── config          # Spring 配置类
├── common          # 跨模块通用类型
├── web             # HTTP 层（Filter、ExceptionHandler）
├── auth            # 认证功能模块
│   ├── controller
│   ├── service
│   ├── dao
│   ├── dao.impl
│   ├── mapper
│   ├── entity
│   ├── dto
│   └── vo
├── user            # 用户功能模块（同 auth 结构，部分模块可能缺少 dto）
├── cdc             # MySQL binlog CDC（独立于分层架构）
│   ├── config
│   ├── engine
│   ├── dispatcher
│   ├── handler
│   └── event
└── infra           # 基础设施
    ├── persistence
    └── redis
```

- 全部小写，不使用下划线
- 功能模块按业务域划分（`auth`、`user`），不是按技术层划分

### 2.2 类与接口命名

| 类型 | 命名模式 | 示例 |
|------|---------|------|
| Service | 名词，无后缀 | `AuthService`, `UserService` |
| DAO 接口 | 名词，无后缀 | `ApiTokenDao`, `UserDao` |
| DAO 实现类 | 接口名 + `Impl` | `ApiTokenDaoImpl`, `UserDaoImpl` |
| Controller | 名词 + `Controller` | `AuthController` |
| 实体 | 名词 + `Entity` | `ApiTokenEntity`, `UserEntity` |
| Mapper | 名词 + `Mapper` | `ApiTokenMapper` |
| DTO（请求/命令） | 名词，无后缀 | `LoginRequest` |
| VO（响应） | 名词 + `Vo` | `LoginVo`, `CurrentUserVo` |
| 配置类 | 名词 + `Config` | `SaTokenConfig` |
| 异常类 | 名词 + `Exception` | `BusinessException` |
| 错误码枚举 | 名词 | `CommonErrorCode` |
| 测试类 | 被测类名 + `Test` | `ApiTokenServiceTest` |

> **Service 为什么不定义接口？** 除非存在多实现需求（如策略模式、A/B 测试），否则单一实现类的接口属于过度设计（YAGNI）。Service 层直接使用类，减少无意义间接层。

### 2.3 方法命名

- 查询：`findXxx`, `getXxx`, `listXxx`
- 创建：`createXxx` 或 `saveXxx`
- 更新：`updateXxx`
- 删除：`deleteXxx`（硬删除）或 `removeXxx`（软删除）
- 验证：`verifyXxx`, `checkXxx`
- 布尔判断：`isXxx`, `hasXxx`

### 2.4 变量与常量

- 局部变量/参数：camelCase，如 `tokenHash`
- 类常量：`static final` + 大写下划线，如 `DEFAULT_PAGE_SIZE`
- 禁止单字母变量（循环索引 `i`/`j` 除外）

### 2.5 数据库命名

- 表名：小写下划线，复数或集合语义，如 `sys_user`, `api_token`
- 字段名：小写下划线，如 `token_hash`, `created_at`
- 索引名：`idx_表名_字段名` 或 `uk_表名_字段名`

---

## 3. API 设计规范

### 3.1 响应格式

所有 API 响应统一使用 `ApiResponse<T>` 包装：

```json
{
  "code": "0",
  "message": "success",
  "data": { ... }
}
```

- `code = "0"` 表示请求处理成功（HTTP 200）
- `code != "0"` 表示业务错误或系统错误
- `null` 字段不输出（Jackson `non_null` 配置）

### 3.2 HTTP 状态码

| 场景 | HTTP Status | code |
|------|-------------|------|
| 成功 | 200 | `0` |
| 参数校验失败 | 400 | `COMMON_400` |
| 未认证 | 401 | `COMMON_401` |
| 无权限 | 403 | `COMMON_403` |
| 资源不存在 | 404 | `COMMON_404` |
| 资源冲突/锁定占用 | 409 | `COMMON_409` / `LOCK_409` |
| 系统内部错误 | 500 | `COMMON_500` |

### 3.3 错误码格式

```
CATEGORY_NUMBER
```

- `CATEGORY`：大写模块名，如 `COMMON`, `AUTH`, `USER`, `LOCK`
- `NUMBER`：3 位数字，对应 HTTP 状态码语义
- 新增错误码必须在 `CommonErrorCode` 或对应模块的枚举中定义

### 3.4 端点路径规范

| 类别 | 路径前缀 | 认证方式 |
|------|---------|---------|
| 公开 | `/api/public/**` | 无 |
| Open API | `/api/open/**` | `X-API-Token` 头 |
| 需登录 | `/api/app/**` | Sa-Token Session |

- Controller 的 `@RequestMapping` 必须包含上述前缀之一
- 路径使用小写、连字符，如 `/api/user/me`，不使用驼峰

### 3.5 分页（预留）

未来分页接口统一使用 MyBatis-Plus 的 `Page<T>`，响应封装为：

```json
{
  "code": "0",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

---

## 4. 数据库与 Flyway 规范

### 4.1 表设计

- 字符集：`utf8mb4`，排序规则：`utf8mb4_0900_ai_ci`
- 每个表必须包含以下审计字段：

```sql
CREATE TABLE example (
    id BIGINT NOT NULL PRIMARY KEY,
    -- 业务字段 ...
    created_at DATETIME NOT NULL,
    created_by BIGINT NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by BIGINT NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    -- 索引 ...
);
```

- 主键：`BIGINT`，不使用自增（使用雪花 ID 或业务分配）
- 软删除：`deleted TINYINT(1) DEFAULT 0`，配合 MyBatis-Plus `@TableLogic`
- 状态字段：`VARCHAR(32)`，值使用全大写，如 `ENABLED`, `DISABLED`
- 时间字段：`DATETIME`，不使用 `TIMESTAMP`

### 4.2 Flyway 迁移脚本

- 位置：`src/main/resources/db/migration/`
- 命名：`V{版本号}__{描述}.sql`，如 `V1__init_schema.sql`, `V2__add_user_status.sql`
- 描述使用小写下划线
- 每个脚本必须可重复执行（幂等）或保证只执行一次（通过 Flyway 版本控制）
- **禁止**修改已发布的迁移脚本（已执行过的 `.sql` 文件）
- 需要修正历史迁移时，创建新的迁移脚本执行修正

### 4.3 数据初始化

- 本地开发种子数据放在 Flyway 迁移中，但必须在代码注释中标注 `LOCAL-ONLY`
- 生产环境不使用 Flyway 初始化业务数据，只用于 schema 迁移

---

## 5. 分层与依赖规则

### 5.1 强制分层

```
Controller -> Service -> DAO -> DAO Impl -> Mapper -> Database
```

- Controller 只依赖 Service、DTO、VO
- Service 只依赖 DAO 和其他 Service，**禁止**直接依赖 Mapper
- DAO Impl 是**唯一**可以依赖 Mapper 的业务层代码
- 实体（Entity）**禁止**作为 API 响应返回

### 5.2 ArchUnit 验证

以上规则由 `LayerArchitectureTest` 强制执行。如果新增模块或调整包结构导致规则失效，必须同步更新 ArchUnit 测试。

### 5.3 CDC 模块豁免

`cdc` 包不参与 Controller→Service→DAO 分层架构。它由数据库 binlog 事件触发，不处理 HTTP 请求，因此不受上述分层规则约束。

---

## 6. 测试规范

### 6.1 测试分类

| 类型 | 位置 | 依赖 |
|------|------|------|
| 单元测试 | `src/test/java/.../service/` | 无外部服务，纯 Mockito |
| MVC 测试 | `src/test/java/.../web/` | 无外部服务，Spring MVC Test |
| 架构测试 | `src/test/java/.../architecture/` | 无外部服务，ArchUnit |
| 集成测试 | `src/test/java/.../integration/` | 需要 MySQL + Redis |

### 6.2 集成测试规范

- 使用 `@ActiveProfiles("test")` + `application-test.yml`
- 数据库隔离：`atlas_mountain_test`，禁止连接开发数据库
- Flyway 在 `@BeforeAll` 中执行 `clean()` + `migrate()`
- 每个测试类标注 `@DirtiesContext(classMode = AFTER_CLASS)`
- 测试数据使用固定 ID（如 9991, 9992）避免与种子数据冲突
- `@AfterEach` 清理测试插入的数据

---

## 7. 日志规范

- 使用 SLF4J + Logback
- 推荐使用 `@Slf4j` 注解（见第 8 节 Lombok 规范）
- 手动声明时：`private static final Logger log = LoggerFactory.getLogger(Xxx.class);`
- 禁止 `System.out.println` 或 `e.printStackTrace()`
- 请求日志由 `RequestLogFilter` 统一处理，业务代码不手动打印请求信息

---

## 8. Lombok 使用规范

项目允许有限使用 Lombok，规则如下：

| 场景 | 允许的注解 | 说明 |
|------|-----------|------|
| Entity | `@Getter`, `@Setter` | MyBatis-Plus 要求 class，用 Lombok 减少样板代码 |
| Service 构造器 | `@RequiredArgsConstructor` | 替代手写构造器注入 |
| 日志 | `@Slf4j` | 替代手写 `LoggerFactory.getLogger(...)` |

**禁止**：
- DTO/VO 使用 `@Data`、`@Getter`、`@Setter` — DTO/VO 必须是 record
- 任何类使用 `@Builder`、`@ToString`、`@EqualsAndHashCode`
- `@Data` 用于 Entity（会生成 `equals`/`hashCode`，可能与 MyBatis-Plus 冲突）

---

## 9. 安全约束

- **禁止**引入 Spring Security Web 栈，仅使用 `spring-security-crypto` 做 BCrypt
- **禁止**在日志、错误消息、API 响应中输出密码、Token、密钥等敏感信息
- **禁止**在代码或配置文件中硬编码生产环境凭据
- 开发种子账户（`admin`/`atlas-local`）仅限本地使用，必须阻止其进入生产环境

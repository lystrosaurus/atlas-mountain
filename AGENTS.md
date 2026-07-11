<!-- From: C:\project\atlas-mountain\AGENTS.md -->
# Atlas Mountain — Agent Guide

> 面向 AI coding agent 的项目速查手册。首次介入请先通读，随后按需查阅。

---

## 项目速览

Spring Boot 4 单体后端模板，Java 21，严格分层（ArchUnit 强制）。

| 项 | 值 |
|---|---|
| Group / Artifact | `io.github.lystrosaurus` / `atlas-mountain` |
| Java / Spring Boot | 21 / 4.0.6 |
| 端口 | 8080 |
| 基础包 | `io.github.lystrosaurus.atlasmountain` |

**核心能力**：Sa-Token 会话 + API Token 双认证、用户管理、Redisson 分布式锁、MyBatis-Plus + Flyway。

---

## Agent 行为原则

> 源自 Andrej Karpathy 对 LLM 编码问题的观察：模型常在未确认的情况下做错误假设、过度复杂化代码、在无关改动中引入副作用。以下四条原则用于约束这些倾向，优先级高于执行速度。

### 1. 先思后写（Think Before Coding）

**不要假设，不要隐藏困惑，呈现权衡。**

- 不确定时**先提问**，不要猜测
- 存在多种理解时**列出选项**，不要静默选择一种
- 若存在更简单方案，**主动提出**
- 困惑时**停下来**，明确指出不清楚的地方并请求澄清

#### 深度思考工具（强制）

接收任何任务后，**必须先使用 `sequential-thinking` 工具**进行结构化思考，再开始实施。

思考内容应覆盖：
- 任务理解：用户真正想要什么？有无歧义？
- 技术方案：现有代码如何实现？有无相似模式可复用？
- 风险识别：会触动哪些模块？有无边界条件或并发风险？
- 实现步骤：拆分为可验证的小步骤
- 验证方式：如何确认实现正确？

**思考深度按任务复杂度调整**：
- 简单任务（修 typo、单行改动）：1-2 步快速确认理解即可
- 常规任务（新接口、bug 修复）：3-5 步结构化分析
- 复杂任务（跨模块重构、架构决策）：5-10 步深度推演，允许回溯和修正

### 2. 极简优先（Simplicity First）

**最小代码解决问题，不做推测性实现。**

- 不实现超出需求的功能
- 不为一次性代码做抽象
- 不添加未被要求的"灵活性"或"可配置性"
- 不为不可能发生的场景写错误处理
- 200 行能写成 50 行，就重写

> **检验标准**：资深工程师看了会觉得过度设计吗？如果是，简化。
>
> **抽象判定**：重复出现 3 次以上的模式再考虑通用化，禁止过早抽象。

### 3. 精准改动（Surgical Changes）

**只碰必须碰的，只清理自己制造的。**

- 不"改进"与任务无关的相邻代码、注释或格式
- 不重构没有坏掉的代码
- 匹配现有风格，即使你自己做法不同
- 发现无关死代码，**提及即可，不要删除**
- 你的改动产生的无用 import / 变量 / 函数，**必须清理**

> **检验标准**：每一行改动都能追溯到用户的明确请求。

### 4. 目标驱动（Goal-Driven Execution）

**定义成功标准，循环验证直到达成。**

将指令式任务转化为可验证的目标：

| 而不是... | 转化为... |
|---|---|
| "添加校验" | "先写无效输入的测试，再让它们通过" |
| "修复 bug" | "先写复现 bug 的测试，再让它通过" |
| "重构 X" | "确保重构前后测试都通过" |

多步骤任务应给出简要计划：

```
1. [步骤] → 验证：[检查方式]
2. [步骤] → 验证：[检查方式]
```

**对本项目的具体化**：
- 声称完成前必须执行 `mvn test`
- 新增功能先写测试（或至少与实现同步）
- 每次提交前执行 `mvn spotless:apply`

**失败熔断**：同一问题连续失败 3 次后，必须暂停并重新评估策略，而非盲目重试。

---

## 常用命令

```bash
mvn test                          # 全部测试（集成测试需本地 MySQL + Redis）
mvn test -Dtest="*IntegrationTest" # 仅集成测试
mvn spotless:apply                # 自动格式化（提交前必须执行）
mvn spring-boot:run -Dspring-boot.run.profiles=local # 本地启动（需 MySQL + Redis）
```

---

## 本地服务

- **MySQL**：`atlas_mountain`（开发）、`atlas_mountain_test`（测试），用户 `atlas`/`atlas`（见 README.md 建库脚本）
- **Redis**：`localhost:6379`
- **Profile**：`local`（从 `dev/application-local.yml.example` 复制到 `config/application-local.yml`，不入库、不打包）

---

## 代码结构

```
config    # Spring Bean（Jackson、MyBatis-Plus、Sa-Token、Web）
common    # ApiResponse<T>、ErrorCode、BusinessException
web       # GlobalExceptionHandler、RequestLogFilter
auth      # 认证模块（controller/service/dao/dao.impl/mapper/entity/dto/vo）
user      # 用户模块（controller/service/dao/dao.impl/mapper/entity/vo）
cdc       # MySQL binlog CDC（config/engine/dispatcher/handler/event）
infra     # persistence（BaseEntity、AuditMetaObjectHandler）、redis（分布式锁）
ops       # 运维占位
```

### 包规则
- 全小写、无下划线；按业务域组织（`auth`、`user`），不按技术层。
- 每个功能模块固定包含：`controller`、`service`、`dao`、`dao.impl`、`mapper`、`entity`、`dto`、`vo`（部分早期模块可能缺少 `dto`）。
- `cdc` 模块不参与 Controller→Service→DAO 分层，由数据库 binlog 事件触发。

---

## 架构规则（ArchUnit 强制）

```
Controller -> Service -> DAO -> DAO Impl -> Mapper -> Database
```

1. **Controller 禁止依赖 DAO、Mapper、Entity**
2. **Service 禁止直接依赖 Mapper**
3. **仅 `dao.impl` 可依赖 Mapper**（以及 `config`、`mapper` 包）
4. **Entity 禁止作为 API 响应** — 必须映射为 VO

新增模块或调整包结构时，**必须同步更新** `LayerArchitectureTest`。

---

## 认证体系

| 前缀 | 认证方式 | 示例 |
|------|---------|------|
| `/api/public/**` | 无 | `/api/public/ping` |
| `/api/open/**` | `X-API-Token` header | `/api/open/ping` |
| 其余路径 | Sa-Token session | `/api/app/ping`, `/api/app/me` |

- **Session**：`StpUtil.login()` / `StpUtil.checkLogin()`，登录端点 `/api/auth/login`
- **API Token**：SHA-256 哈希存储，格式 `ak_<prefix>_<secret>`
- **密码**：BCrypt（`spring-security-crypto`，**禁止引入 Spring Security Web 栈**）
- **显式放行**：`/api/auth/login`、`/actuator/health`、`/actuator/health/**`；`/error` 仅供内部错误分派

> **开发种子账户**：`admin` / `atlas-local`（仅本地，禁止进入生产环境）

---

## API 规范

### 响应格式

```json
{
  "code": "0",
  "message": "success",
  "data": { ... }
}
```

- `code = "0"` → 成功（HTTP 200）
- `code != "0"` → 业务/系统错误
- `null` 字段不输出（Jackson `non_null`）

### 错误码格式

```
CATEGORY_NUMBER
```

示例：`COMMON_400`、`AUTH_401`、`LOCK_409`。新增错误码放在 `CommonErrorCode` 或模块枚举中。

### HTTP 状态映射

| 场景 | HTTP | code |
|------|------|------|
| 成功 | 200 | `0` |
| 参数校验失败 | 400 | `COMMON_400` |
| 未认证 | 401 | `COMMON_401` |
| 无权限 | 403 | `COMMON_403` |
| 资源不存在 | 404 | `COMMON_404` |
| 冲突/锁占用 | 409 | `COMMON_409` / `LOCK_409` |
| 内部错误 | 500 | `COMMON_500` |

---

## 编码约束

### 风格（Spotless 强制执行）

| 规则 | 值 |
|------|-----|
| 缩进 | 2 空格 |
| 换行 | LF |
| 编码 | UTF-8 无 BOM |
| 最大行宽 | 120 |
| 大括号 | K&R |
| 导入顺序 | `java,javax,jakarta,org,com,` |

### 命名速查

| 类型 | 模式 | 示例 |
|------|------|------|
| 接口 | 名词 | `ApiTokenService` |
| 实现 | 接口 + `Impl` | `ApiTokenServiceImpl` |
| Controller | 名词 + `Controller` | `AuthController` |
| Entity | 名词 + `Entity` | `UserEntity` |
| Mapper | 名词 + `Mapper` | `UserMapper` |
| DTO | 名词 | `LoginRequest` |
| VO | 名词 + `Vo` | `LoginVo` |
| 测试 | 被测类 + `Test` | `AuthServiceImplTest` |

### 方法命名

- 查询：`findXxx`、`getXxx`、`listXxx`
- 创建：`createXxx` / `saveXxx`
- 更新：`updateXxx`
- 删除：`deleteXxx`（硬）/ `removeXxx`（软）
- 验证：`verifyXxx`、`checkXxx`
- 布尔：`isXxx`、`hasXxx`

### 编码模式

- **构造器注入**，禁止字段 `@Autowired`
- **DTO/VO 用 record**，Entity 用 class（MyBatis-Plus 要求）
- **显式 `public`**，不用 `var`
- **Lombok**：仅限 Entity 的 `@Getter`/`@Setter`、日志 `@Slf4j`、构造器 `@RequiredArgsConstructor`；DTO/VO 保持 record，禁止 `@Data` 用于 DTO/VO
- **不用 `System.out.println`**，用 SLF4J

---

## 数据库规范

- 字符集：`utf8mb4`，排序：`utf8mb4_0900_ai_ci`
- 主键：`BIGINT`，**不自增**（雪花 ID 或业务分配）
- 软删除：`deleted TINYINT(1) DEFAULT 0` + `@TableLogic`
- 审计字段（每个表必须）：`created_at`、`created_by`、`updated_at`、`updated_by`、`deleted`
- 状态字段：`VARCHAR(32)`，值全大写（`ENABLED`、`DISABLED`）
- 时间字段：`DATETIME`
- 表名：小写下划线、复数语义（`sys_user`、`api_token`）
- 索引名：`idx_表名_字段名`、`uk_表名_字段名`

### Flyway

- 位置：`src/main/resources/db/migration/`
- 命名：`V{version}__{description}.sql`，描述小写下划线
- **禁止修改已发布脚本**，修正历史问题需新建迁移
- 本地种子数据允许放在迁移中，但必须标注 `LOCAL-ONLY`

---

## 测试策略

| 类型 | 位置 | 依赖 |
|------|------|------|
| 单元 | `.../service/`、`.../web/`、`.../infra/` | 纯 Mockito |
| MVC | `.../web/` | Spring MVC Test |
| 架构 | `.../architecture/` | ArchUnit |
| 集成 | `.../integration/` | MySQL + Redis |

### 集成测试基类

- `IntegrationTestBase`：`@SpringBootTest(RANDOM_PORT)` + `@ActiveProfiles("test")` + Flyway `clean()` + 公共/本地迁移 `migrate()`
- `MockMvcIntegrationTest`：提供 `mockMvc` + `jdbcTemplate`，用于端点级集成测试
- 测试数据用固定 ID（如 `9991`、`9992`），`@AfterEach` 清理

---

## 安全红线

1. **禁止引入 Spring Security Web 栈**（仅用 `spring-security-crypto`）
2. **禁止在日志/响应中输出密码、Token、密钥**
3. **禁止硬编码生产环境凭据**
4. 开发种子账户仅限本地
5. API Token 仅比较 SHA-256 哈希，不比较明文

---

## 关键文件速查

| 文件 | 作用 |
|------|------|
| `pom.xml` | Maven 依赖与构建配置 |
| `CODING_STANDARDS.md` | 完整中文编码规范（风格、命名、API、DB、测试、安全） |
| `CLAUDE.md` | 极简速查（架构、命令、约束） |
| `.editorconfig` | 编辑器格式规则 |
| `application.yml` | 基础配置 |
| `dev/application-local.yml.example` | 本地开发配置模板；复制到外部 `config/application-local.yml` |
| `application-test.yml` | 测试配置（随机端口、独立数据库） |
| `db/migration/V1__init_schema.sql` | 初始 schema；含历史开发种子，后续由 V2 移除 |
| `db/migration/V2__remove_local_development_account.sql` | 从公共迁移结果移除开发账户 |
| `dev/db/local/R__local_development_account.sql` | 仅 local/test 通过 filesystem 位置加载的开发账户，不打包 |
| `LayerArchitectureTest.java` | ArchUnit 分层强制 |
| `IntegrationTestBase.java` | 集成测试基类 |
| `MockMvcIntegrationTest.java` | MockMvc + JdbcTemplate 集成测试基类 |

---

## SonarQube Cloud 工作流程

当用户说 **"看看 SonarQube 上的问题"** 时，执行以下步骤：

1. **拉取开放问题** — 调用 SonarQube Cloud API：
   ```
   https://sonarcloud.io/api/issues/search?componentKeys=lystrosaurus_atlas-mountain&ps=500&statuses=OPEN,CONFIRMED&types=BUG,VULNERABILITY,CODE_SMELL
   ```
2. **展示结果** — 按严重程度排序（BLOCKER > CRITICAL > MAJOR > MINOR > INFO），表格包含：规则、严重程度、文件、行号、说明。
3. **等待确认** — 用户说"修复"后再应用修复。

**已知排除项**（不报告）：
- `secrets:S8215` 在 `V1__init_schema.sql` — 已在 SonarQube Cloud 项目设置中排除（LOCAL-ONLY dev seed password）
- `plsql:*` 规则在 `*.sql` 文件 — 误报（MySQL 被误识别为 Oracle PL/SQL）

**项目 URL**：https://sonarcloud.io/project/overview?id=lystrosaurus_atlas-mountain

---

## Agent 行动清单

1. **任何任务先使用 sequential-thinking** — 先思考再动手，复杂度决定思考深度
2. **提交前执行 `mvn spotless:apply`** — 格式错误会导致构建失败
3. **声称完成前执行 `mvn test`** — 集成测试需本地 MySQL + Redis
4. **新增模块时镜像 `auth`/`user` 结构**，并更新 ArchUnit 测试
5. **DTO/VO 用 record，Entity 用 class**
6. **构造器注入，不用字段注入**
7. **Lombok 仅限 Entity `@Getter`/`@Setter`、`@Slf4j`、`@RequiredArgsConstructor`**
8. **不引入 Spring Security Web 栈**
9. **Controller 不返回 Entity，必须映射为 VO**
10. **Service 不绕过 DAO 直接调 Mapper**
11. **错误码遵循 `CATEGORY_NUMBER` 格式**
12. **用户说"看看 SonarQube 上的问题"时** — 调用 SonarQube Cloud API 拉取问题列表，展示给用户确认后再修复

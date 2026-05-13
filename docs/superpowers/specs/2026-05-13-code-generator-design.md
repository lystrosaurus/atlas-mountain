# Atlas Mountain 代码生成器设计

> 为 Atlas Mountain 项目提供数据库反向生成代码能力，生成符合项目编码规范和 ArchUnit 分层架构的样板代码。

---

## 1. 背景与目标

Atlas Mountain 采用严格的分层架构（Controller → Service → DAO → DAO Impl → Mapper），所有新增业务模块都需要手写大量样板代码（Entity、Mapper、DAO、DAO Impl）。引入代码生成器可以减少重复劳动，同时保证生成的代码符合 `CODING_STANDARDS.md` 和 ArchUnit 规则。

**目标**：
- 从 MySQL 数据库表反向生成代码
- 生成代码必须符合 Atlas Mountain 命名规范、包结构和分层规则
- 不污染生产代码（生成器仅作为开发辅助工具）
- 配置外部化，支持不同开发者使用不同数据库/输出路径

---

## 2. 运行方式

**方案**：纯 Java Main 类，不依赖 Spring 容器启动。

- 入口类：`src/test/java/io/github/lystrosaurus/atlasmountain/ops/CodeGenerator.java`
- 配置：`src/test/resources/application-generator.yml`
- 运行：`mvn exec:java -Dexec.mainClass="...CodeGenerator" -Dexec.classpathScope=test`
- 输出：外部目录（由配置指定），开发者手动确认后复制到 `src/main/java`

**理由**：
- 代码生成是一次性开发辅助行为，不应出现在生产运行时
- 不启动 Spring 上下文，执行速度快
- 放在 `src/test` 下不打包进生产 jar

---

## 3. 生成范围

**生成 4 个文件**（最小集）：

| 文件 | 模板 | 说明 |
|------|------|------|
| `XxxEntity.java` | `entity.java.vm` | 继承 `BaseEntity`，`@TableName`，`@Getter`/`@Setter` |
| `XxxMapper.java` | `mapper.java.vm` | 继承 `BaseMapper<XxxEntity>` |
| `XxxDao.java` | `dao.java.vm` | 自定义接口，预生成 `findById` |
| `XxxDaoImpl.java` | `daoImpl.java.vm` | `@Repository`，构造器注入 Mapper |

**不生成**（由开发者后续手动补充）：Service、ServiceImpl、Controller、VO、DTO、MapStruct Mapper、XML。

---

## 4. 技术选型

| 组件 | 选择 | 说明 |
|------|------|------|
| 元数据读取 | MyBatis-Plus `FastAutoGenerator` | 成熟的数据库元数据读取、类型映射、字段命名转换 |
| 模板引擎 | Velocity | MP Generator 默认支持，模板语法简洁 |
| Entity/Mapper 生成 | MP Generator + 自定义模板 | 复用 MP 的类型转换和字段处理 |
| DAO/DAO Impl 生成 | 自定义 Velocity 模板 | MP 的 Service/ServiceImpl 语义不匹配 Atlas Mountain 的 DAO 模式 |

**新增依赖**（`pom.xml`，`test` scope）：
- `com.baomidou:mybatis-plus-generator`
- `org.apache.velocity:velocity-engine-core`

---

## 5. 配置设计

### `application-generator.yml`

```yaml
generator:
  datasource:
    url: jdbc:mysql://localhost:3306/atlas_mountain?remarks=true&useInformationSchema=true
    username: root
    password: ""

  output-dir: /tmp/generated

  package:
    parent: io.github.lystrosaurus.atlasmountain
    module: order

  tables:
    - sys_order
    - sys_order_item
```

### 配置项说明

| 配置项 | 必填 | 说明 |
|--------|------|------|
| `generator.datasource.url` | 是 | JDBC URL，需加 `remarks=true&useInformationSchema=true` 读取注释 |
| `generator.datasource.username` | 是 | 数据库用户名 |
| `generator.datasource.password` | 否 | 数据库密码 |
| `generator.output-dir` | 是 | 代码输出目录（外部目录，避免覆盖现有代码） |
| `generator.package.parent` | 是 | 父包名 |
| `generator.package.module` | 是 | 模块包名，如 `order` → `...order.entity` |
| `generator.tables` | 是 | 表名列表，显式指定 |

---

## 6. 模板设计

### 6.1 Entity 模板

生成的 `OrderEntity`：

```java
package io.github.lystrosaurus.atlasmountain.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.lystrosaurus.atlasmountain.infra.persistence.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@TableName("sys_order")
@Getter
@Setter
public class OrderEntity extends BaseEntity {

  @TableId private Long id;
  private String orderNo;
  private java.math.BigDecimal amount;
  private String status;
}
```

**规则**：
- 继承 `BaseEntity`（含审计字段和逻辑删除）
- `@TableId private Long id;` 单独声明（不依赖 `BaseEntity` 的 id）
- Lombok 只用 `@Getter` + `@Setter`
- 字段注释来自数据库备注

### 6.2 Mapper 模板

```java
package io.github.lystrosaurus.atlasmountain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.lystrosaurus.atlasmountain.order.entity.OrderEntity;

public interface OrderMapper extends BaseMapper<OrderEntity> {}
```

### 6.3 DAO 接口模板

```java
package io.github.lystrosaurus.atlasmountain.order.dao;

import java.util.Optional;
import io.github.lystrosaurus.atlasmountain.order.entity.OrderEntity;

public interface OrderDao {

  Optional<OrderEntity> findById(Long id);
}
```

### 6.4 DAO Impl 模板

```java
package io.github.lystrosaurus.atlasmountain.order.dao.impl;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import io.github.lystrosaurus.atlasmountain.order.mapper.OrderMapper;
import io.github.lystrosaurus.atlasmountain.order.entity.OrderEntity;
import io.github.lystrosaurus.atlasmountain.order.dao.OrderDao;

@Repository
public class OrderDaoImpl implements OrderDao {

  private final OrderMapper orderMapper;

  public OrderDaoImpl(OrderMapper orderMapper) {
    this.orderMapper = orderMapper;
  }

  @Override
  public Optional<OrderEntity> findById(Long id) {
    return Optional.ofNullable(orderMapper.selectById(id));
  }
}
```

---

## 7. 命名与类型映射

### 7.1 表名 → 类名

| 表名 | 类名前缀 | 生成类 |
|------|---------|--------|
| `sys_order` | `Order` | `OrderEntity`, `OrderMapper`, `OrderDao`, `OrderDaoImpl` |
| `t_bi_monthly_review` | `MonthlyReview` | `MonthlyReviewEntity`, ... |
| `api_token` | `ApiToken` | `ApiTokenEntity`, ... |

**规则**：去掉 `sys_`、`t_`、`t_bi_` 等常见前缀，剩余部分转大驼峰。

### 7.2 数据库类型 → Java 类型

| MySQL 类型 | Java 类型 |
|-----------|-----------|
| `BIGINT` | `Long` |
| `VARCHAR`, `TEXT` | `String` |
| `DECIMAL` | `java.math.BigDecimal` |
| `DATETIME` | `java.time.LocalDateTime` |
| `TINYINT(1)` | `Boolean` |
| `TINYINT`（非 1） | `Integer` |
| `INT` | `Integer` |

### 7.3 排除字段

以下字段由 `BaseEntity` 提供，不在 Entity 中重复生成：
- `created_at` → `BaseEntity.createdAt`
- `created_by` → `BaseEntity.createdBy`
- `updated_at` → `BaseEntity.updatedAt`
- `updated_by` → `BaseEntity.updatedBy`
- `deleted` → `BaseEntity.deleted`（`@TableLogic`）

---

## 8. 输出目录结构

以 `module: order`，表 `sys_order` 为例：

```
/tmp/generated/
└── io/
    └── github/
        └── lystrosaurus/
            └── atlasmountain/
                └── order/
                    ├── entity/
                    │   └── OrderEntity.java
                    ├── mapper/
                    │   └── OrderMapper.java
                    ├── dao/
                    │   └── OrderDao.java
                    └── dao/
                        └── impl/
                            └── OrderDaoImpl.java
```

---

## 9. 使用流程

1. 在 `application-generator.yml` 中配置数据库连接、模块名、表名
2. 运行 `mvn exec:java ...`
3. 检查 `/tmp/generated/` 下的生成结果
4. 手动将文件复制到 `src/main/java` 对应位置
5. 运行 `mvn spotless:apply` 格式化
6. 运行 `mvn test` 确保 ArchUnit 分层规则通过

---

## 10. 风险与限制

| 风险 | 缓解措施 |
|------|---------|
| 生成代码覆盖已有文件 | 输出到外部目录，强制手动确认后再复制 |
| 表前缀识别不准确 | 支持配置自定义前缀规则，或后续手动调整类名 |
| DAO 接口只有 `findById` | 这是预期行为，其他业务查询方法需开发者手动添加 |
| 配置中的数据库密码明文 | 仅限本地开发使用，不提交生产凭据 |

---

## 11. 后续扩展（不在本次范围）

- 支持生成 Service + ServiceImpl + Controller + VO（标准集）
- 支持生成 DTO + MapStruct Mapper（完整集）
- 支持前缀匹配/正则匹配选择表
- 支持 Flyway 迁移脚本逆向生成

# Code Generator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a command-line code generator (in `src/test`) that reverse-engineers MySQL tables into Atlas Mountain-compliant Entity, Mapper, DAO, and DAO Impl files.

**Architecture:** MyBatis-Plus `FastAutoGenerator` generates Entity and Mapper via custom Velocity templates. A custom JDBC-based generator produces DAO and DAO Impl using separate Velocity templates. All files output to an external directory for manual review before copying into `src/main/java`.

**Tech Stack:** MyBatis-Plus Generator 3.5.16, Velocity 2.4, YAML configuration

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `pom.xml` | Modify | Add `mybatis-plus-generator` and `velocity-engine-core` dependencies (test scope) |
| `src/test/resources/application-generator.yml` | Create | Database connection, output directory, module name, table list |
| `src/test/resources/templates/entity.java.vm` | Create | Velocity template for Entity classes |
| `src/test/resources/templates/mapper.java.vm` | Create | Velocity template for Mapper interfaces |
| `src/test/resources/templates/dao.java.vm` | Create | Velocity template for DAO interfaces |
| `src/test/resources/templates/daoImpl.java.vm` | Create | Velocity template for DAO implementations |
| `src/test/java/io/github/lystrosaurus/atlasmountain/ops/CodeGenerator.java` | Create | Main entry point: reads config, orchestrates generation |

---

## Task 1: Add Maven Dependencies

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add `mybatis-plus-generator` dependency**

Insert after the existing `mybatis-plus` dependency in `pom.xml`:

```xml
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-generator</artifactId>
  <version>${mybatis-plus.version}</version>
  <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Add `velocity-engine-core` dependency**

Insert after the `mybatis-plus-generator` dependency:

```xml
<dependency>
  <groupId>org.apache.velocity</groupId>
  <artifactId>velocity-engine-core</artifactId>
  <version>2.4.1</version>
  <scope>test</scope>
</dependency>
```

- [ ] **Step 3: Verify dependencies compile**

Run:
```bash
mvn dependency:resolve -DincludeScope=test
```

Expected: Both dependencies resolve without error.

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "chore: add mybatis-plus-generator and velocity dependencies (test scope)

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 2: Create Configuration File

**Files:**
- Create: `src/test/resources/application-generator.yml`

- [ ] **Step 1: Write configuration file**

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

- [ ] **Step 2: Verify file is on classpath**

Run:
```bash
mvn test-compile
ls -la target/test-classes/application-generator.yml
```

Expected: File exists at `target/test-classes/application-generator.yml`.

- [ ] **Step 3: Commit**

```bash
git add src/test/resources/application-generator.yml
git commit -m "chore: add code generator configuration template

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 3: Create Velocity Templates

**Files:**
- Create: `src/test/resources/templates/entity.java.vm`
- Create: `src/test/resources/templates/mapper.java.vm`
- Create: `src/test/resources/templates/dao.java.vm`
- Create: `src/test/resources/templates/daoImpl.java.vm`

### 3.1 Entity Template

- [ ] **Step 1: Write `entity.java.vm`**

```velocity
package ${package.Entity};

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.lystrosaurus.atlasmountain.infra.persistence.BaseEntity;
import lombok.Getter;
import lombok.Setter;

#if("$!table.comment" != "")
/**
 * $!{table.comment}.
 */
#end
@TableName("${table.name}")
@Getter
@Setter
public class ${entity} extends BaseEntity {

  @TableId private Long id;
#foreach($field in ${table.fields})
#if(!${field.keyFlag})
#if("$!field.comment" != "")
  /**
   * ${field.comment}.
   */
#end
  private ${field.propertyType} ${field.propertyName};
#end
#end
}
```

### 3.2 Mapper Template

- [ ] **Step 2: Write `mapper.java.vm`**

```velocity
package ${package.Mapper};

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${package.Entity}.${entity};

#if("$!table.comment" != "")
/**
 * $!{table.comment} Mapper 接口.
 */
#end
public interface ${table.mapperName} extends BaseMapper<${entity}> {
}
```

### 3.3 DAO Template

- [ ] **Step 3: Write `dao.java.vm`**

```velocity
package ${packageName}.dao;

import java.util.Optional;
import ${packageName}.entity.${className}Entity;

#if($!tableComment != "")
/**
 * $tableComment DAO 接口.
 */
#end
public interface ${className}Dao {

  Optional<${className}Entity> findById(Long id);
}
```

### 3.4 DAO Impl Template

- [ ] **Step 4: Write `daoImpl.java.vm`**

```velocity
package ${packageName}.dao.impl;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import ${packageName}.mapper.${className}Mapper;
import ${packageName}.entity.${className}Entity;
import ${packageName}.dao.${className}Dao;

#if($!tableComment != "")
/**
 * $tableComment DAO 实现类.
 */
#end
@Repository
public class ${className}DaoImpl implements ${className}Dao {

  private final ${className}Mapper ${mapperVariable};

  public ${className}DaoImpl(${className}Mapper ${mapperVariable}) {
    this.${mapperVariable} = ${mapperVariable};
  }

  @Override
  public Optional<${className}Entity> findById(Long id) {
    return Optional.ofNullable(${mapperVariable}.selectById(id));
  }
}
```

- [ ] **Step 5: Verify templates compile to target**

Run:
```bash
mvn test-compile
ls -la target/test-classes/templates/
```

Expected: All 4 `.vm` files present.

- [ ] **Step 6: Commit**

```bash
git add src/test/resources/templates/
git commit -m "chore: add code generator velocity templates

Templates for Entity, Mapper, DAO, and DAO Impl generation.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 4: Implement CodeGenerator Entry Point

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/ops/CodeGenerator.java`

This is the main orchestrator. It:
1. Reads `application-generator.yml`
2. Uses MP `FastAutoGenerator` for Entity and Mapper
3. Uses custom JDBC + Velocity for DAO and DAO Impl

### 4.1 Generator Config Record

- [ ] **Step 1: Write the configuration record and YAML loading logic**

```java
package io.github.lystrosaurus.atlasmountain.ops;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

record GeneratorConfig(
    String url,
    String username,
    String password,
    String outputDir,
    String parentPackage,
    String module,
    List<String> tables) {

  static GeneratorConfig load() {
    Yaml yaml = new Yaml();
    try (InputStream in =
        GeneratorConfig.class.getResourceAsStream("/application-generator.yml")) {
      if (in == null) {
        throw new IllegalStateException("application-generator.yml not found on classpath");
      }
      Map<String, Object> root = yaml.load(in);
      @SuppressWarnings("unchecked")
      Map<String, Object> gen = (Map<String, Object>) root.get("generator");
      @SuppressWarnings("unchecked")
      Map<String, Object> ds = (Map<String, Object>) gen.get("datasource");
      @SuppressWarnings("unchecked")
      Map<String, Object> pkg = (Map<String, Object>) gen.get("package");
      @SuppressWarnings("unchecked")
      List<String> tables = (List<String>) gen.get("tables");

      return new GeneratorConfig(
          (String) ds.get("url"),
          (String) ds.get("username"),
          (String) ds.get("password"),
          (String) gen.get("output-dir"),
          (String) pkg.get("parent"),
          (String) pkg.get("module"),
          tables);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load generator config", e);
    }
  }
}
```

### 4.2 Entity and Mapper Generation (MP FastAutoGenerator)

- [ ] **Step 2: Write MP Generator setup for Entity and Mapper**

```java
  private static void generateEntityAndMapper(GeneratorConfig config) {
    String packagePath = config.parentPackage() + "." + config.module();

    FastAutoGenerator.create(config.url(), config.username(), config.password())
        .globalConfig(
            builder ->
                builder
                    .author("generator")
                    .outputDir(config.outputDir())
                    .disableOpenDir())
        .packageConfig(
            builder ->
                builder
                    .parent(config.parentPackage())
                    .moduleName(config.module())
                    .entity("entity")
                    .mapper("mapper")
                    .service("skip")
                    .serviceImpl("skip")
                    .controller("skip")
                    .xml("mapper.xml"))
        .strategyConfig(
            builder ->
                builder
                    .addInclude(config.tables())
                    .entityBuilder()
                    .enableTableFieldAnnotation()
                    .enableLombok()
                    .idType(IdType.INPUT)
                    .naming(NamingStrategy.underline_to_camel)
                    .columnNaming(NamingStrategy.underline_to_camel)
                    .addIgnoreColumns("created_at", "created_by", "updated_at", "updated_by", "deleted")
                    .mapperBuilder()
                    .enableBaseResultMap()
                    .enableBaseColumnList())
        .templateConfig(
            builder ->
                builder
                    .entity("/templates/entity.java.vm")
                    .mapper("/templates/mapper.java.vm")
                    .disable(TemplateType.SERVICE)
                    .disable(TemplateType.SERVICE_IMPL)
                    .disable(TemplateType.CONTROLLER))
        .execute();
  }
```

**Note:** The above imports `IdType`, `NamingStrategy`, `TemplateType` from MP Generator packages.

### 4.3 DAO and DAO Impl Generation (Custom)

- [ ] **Step 3: Write the custom DAO generator**

```java
  private static void generateDaoAndDaoImpl(GeneratorConfig config) throws Exception {
    VelocityEngine velocity = new VelocityEngine();
    velocity.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
    velocity.setProperty(
        "resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
    velocity.init();

    String packagePath = config.parentPackage() + "." + config.module();

    try (Connection conn =
        DriverManager.getConnection(config.url(), config.username(), config.password())) {
      DatabaseMetaData metaData = conn.getMetaData();

      for (String tableName : config.tables()) {
        TableInfo tableInfo = extractTableInfo(metaData, tableName);
        String className = toClassName(tableName);
        String mapperVariable = className.substring(0, 1).toLowerCase() + className.substring(1) + "Mapper";

        // Generate DAO
        VelocityContext daoContext = new VelocityContext();
        daoContext.put("packageName", packagePath);
        daoContext.put("className", className);
        daoContext.put("tableComment", tableInfo.comment());

        String daoPath =
            config.outputDir()
                + "/"
                + packagePath.replace('.', '/')
                + "/dao/"
                + className
                + "Dao.java";
        renderTemplate(velocity, "/templates/dao.java.vm", daoContext, daoPath);

        // Generate DAO Impl
        VelocityContext implContext = new VelocityContext();
        implContext.put("packageName", packagePath);
        implContext.put("className", className);
        implContext.put("mapperVariable", mapperVariable);
        implContext.put("tableComment", tableInfo.comment());

        String implPath =
            config.outputDir()
                + "/"
                + packagePath.replace('.', '/')
                + "/dao/impl/"
                + className
                + "DaoImpl.java";
        renderTemplate(velocity, "/templates/daoImpl.java.vm", implContext, implPath);
      }
    }
  }
```

### 4.4 Table Metadata Extraction

- [ ] **Step 4: Write table metadata extraction**

```java
  record TableInfo(String name, String comment, List<FieldInfo> fields) {}

  record FieldInfo(String columnName, String propertyName, String javaType, String comment, boolean primaryKey) {}

  private static TableInfo extractTableInfo(DatabaseMetaData metaData, String tableName)
      throws SQLException {
    String comment = "";
    try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
      if (tables.next()) {
        comment = tables.getString("REMARKS");
      }
    }

    List<FieldInfo> fields = new ArrayList<>();
    try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        if (isBaseEntityColumn(columnName)) {
          continue;
        }
        String typeName = columns.getString("TYPE_NAME");
        int dataType = columns.getInt("DATA_TYPE");
        int columnSize = columns.getInt("COLUMN_SIZE");
        String colComment = columns.getString("REMARKS");
        String javaType = toJavaType(typeName, dataType, columnSize);
        String propertyName = toCamelCase(columnName);
        boolean isPk = isPrimaryKey(metaData, tableName, columnName);
        fields.add(new FieldInfo(columnName, propertyName, javaType, colComment, isPk));
      }
    }

    return new TableInfo(tableName, comment != null ? comment : "", fields);
  }

  private static boolean isBaseEntityColumn(String columnName) {
    return "created_at".equals(columnName)
        || "created_by".equals(columnName)
        || "updated_at".equals(columnName)
        || "updated_by".equals(columnName)
        || "deleted".equals(columnName);
  }

  private static boolean isPrimaryKey(DatabaseMetaData metaData, String tableName, String columnName)
      throws SQLException {
    try (ResultSet pks = metaData.getPrimaryKeys(null, null, tableName)) {
      while (pks.next()) {
        if (columnName.equals(pks.getString("COLUMN_NAME"))) {
          return true;
        }
      }
    }
    return false;
  }
```

### 4.5 Type and Name Conversion Utilities

- [ ] **Step 5: Write type and name conversion utilities**

```java
  private static String toJavaType(String typeName, int sqlType, int columnSize) {
    return switch (typeName.toUpperCase()) {
      case "BIGINT" -> "Long";
      case "VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT" -> "String";
      case "DECIMAL", "NUMERIC" -> "java.math.BigDecimal";
      case "DATETIME", "TIMESTAMP" -> "java.time.LocalDateTime";
      case "DATE" -> "java.time.LocalDate";
      case "INT", "INTEGER" -> "Integer";
      case "SMALLINT" -> "Integer";
      case "TINYINT" -> columnSize == 1 ? "Boolean" : "Integer";
      case "BIT" -> "Boolean";
      case "FLOAT" -> "Float";
      case "DOUBLE" -> "Double";
      case "BLOB", "LONGBLOB", "MEDIUMBLOB" -> "byte[]";
      case "JSON" -> "String";
      default -> "Object";
    };
  }

  private static String toClassName(String tableName) {
    String withoutPrefix = tableName;
    for (String prefix : List.of("sys_", "t_", "t_bi_", "tb_")) {
      if (tableName.startsWith(prefix)) {
        withoutPrefix = tableName.substring(prefix.length());
        break;
      }
    }
    return toCamelCase(withoutPrefix, true);
  }

  private static String toCamelCase(String input) {
    return toCamelCase(input, false);
  }

  private static String toCamelCase(String input, boolean capitalizeFirst) {
    StringBuilder result = new StringBuilder();
    boolean nextUpper = capitalizeFirst;
    for (char c : input.toCharArray()) {
      if (c == '_') {
        nextUpper = true;
      } else if (nextUpper) {
        result.append(Character.toUpperCase(c));
        nextUpper = false;
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
```

### 4.6 Template Rendering Utility

- [ ] **Step 6: Write template rendering utility**

```java
  private static void renderTemplate(
      VelocityEngine velocity, String templatePath, VelocityContext context, String outputPath)
      throws Exception {
    Template template = velocity.getTemplate(templatePath, "UTF-8");
    File file = new File(outputPath);
    file.getParentFile().mkdirs();
    try (FileWriter writer = new FileWriter(file)) {
      template.merge(context, writer);
    }
  }
```

### 4.7 Main Method

- [ ] **Step 7: Write main method**

```java
  public static void main(String[] args) throws Exception {
    GeneratorConfig config = GeneratorConfig.load();
    System.out.println("Generating code for tables: " + config.tables());
    System.out.println("Output directory: " + config.outputDir());

    generateEntityAndMapper(config);
    generateDaoAndDaoImpl(config);

    System.out.println("Code generation completed.");
    System.out.println("Please review files in: " + config.outputDir());
  }
```

- [ ] **Step 8: Add required imports at top of file**

```java
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.yaml.snakeyaml.Yaml;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
```

- [ ] **Step 9: Verify CodeGenerator compiles**

Run:
```bash
mvn test-compile
```

Expected: BUILD SUCCESS. `CodeGenerator.class` exists at `target/test-classes/.../ops/CodeGenerator.class`.

- [ ] **Step 10: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/ops/CodeGenerator.java
git commit -m "feat: add CodeGenerator entry point

Orchestrates Entity/Mapper generation via MyBatis-Plus Generator
and DAO/DAO Impl generation via custom JDBC + Velocity.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Task 5: Verify End-to-End

**Files:**
- None (verification only)

Prerequisites: MySQL running locally with `atlas_mountain` database and at least one test table (e.g., `sys_order`).

- [ ] **Step 1: Create a test table (if not exists)**

Connect to MySQL and run:
```sql
CREATE TABLE IF NOT EXISTS sys_order (
    id BIGINT NOT NULL PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by BIGINT NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by BIGINT NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Update config with local database credentials**

Edit `src/test/resources/application-generator.yml` with actual credentials.

- [ ] **Step 3: Run the generator**

```bash
mvn exec:java \
  -Dexec.mainClass="io.github.lystrosaurus.atlasmountain.ops.CodeGenerator" \
  -Dexec.classpathScope=test
```

Expected output:
```
Generating code for tables: [sys_order]
Output directory: /tmp/generated
Code generation completed.
Please review files in: /tmp/generated
```

- [ ] **Step 4: Inspect generated files**

```bash
find /tmp/generated -name "*.java" | sort
```

Expected 4 files:
```
/tmp/generated/.../order/dao/OrderDao.java
/tmp/generated/.../order/dao/impl/OrderDaoImpl.java
/tmp/generated/.../order/entity/OrderEntity.java
/tmp/generated/.../order/mapper/OrderMapper.java
```

- [ ] **Step 5: Verify Entity content**

```bash
cat /tmp/generated/.../order/entity/OrderEntity.java
```

Expected content includes:
- `extends BaseEntity`
- `@TableName("sys_order")`
- `@Getter` and `@Setter`
- `@TableId private Long id;`
- `private String orderNo;`
- `private java.math.BigDecimal amount;`
- `private String status;`
- **No** `createdAt`, `updatedAt`, `deleted` fields (from BaseEntity)

- [ ] **Step 6: Verify DAO Impl content**

```bash
cat /tmp/generated/.../order/dao/impl/OrderDaoImpl.java
```

Expected content includes:
- `@Repository`
- `implements OrderDao`
- `private final OrderMapper orderMapper;`
- Constructor injection
- `Optional.ofNullable(orderMapper.selectById(id))`

- [ ] **Step 7: Verify existing tests still pass**

```bash
mvn test
```

Expected: BUILD SUCCESS (generator code is in `src/test`, does not affect production tests).

- [ ] **Step 8: Commit verification notes (optional)**

If config was modified for local testing, revert before commit:
```bash
git checkout src/test/resources/application-generator.yml
```

---

## Self-Review Checklist

### Spec Coverage

| Spec Requirement | Implementing Task |
|-----------------|-------------------|
| Command-line tool in `src/test` | Task 4 |
| External configuration (`application-generator.yml`) | Task 2 |
| Generate Entity + Mapper + DAO + DAO Impl | Tasks 3, 4 |
| Entity inherits BaseEntity, excludes audit columns | Task 3 (entity template), Task 4 (isBaseEntityColumn) |
| `@TableId private Long id` | Task 3 (entity template) |
| Lombok `@Getter`/`@Setter` only | Task 3 (entity template) |
| Mapper extends BaseMapper | Task 3 (mapper template) |
| DAO with `findById` | Task 3 (dao template) |
| DAO Impl `@Repository`, constructor injection | Task 3 (daoImpl template) |
| Table prefix stripping for class names | Task 4 (toClassName) |
| MySQL type → Java type mapping | Task 4 (toJavaType) |
| Output to external directory | Task 2 (config), Task 4 |
| `test` scope dependencies | Task 1 |

### Placeholder Scan

- [x] No TBD/TODO/fill-in-details
- [x] All code blocks contain complete code
- [x] No vague instructions like "add error handling"
- [x] No "similar to Task N" references

### Type Consistency

- [x] `GeneratorConfig` record fields match usage in all tasks
- [x] `TableInfo`/`FieldInfo` records used consistently
- [x] Template variable names (`className`, `packageName`, etc.) match between Java code and Velocity templates

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-05-13-code-generator.md`.**

Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?

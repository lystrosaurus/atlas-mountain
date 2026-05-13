package io.github.lystrosaurus.atlasmountain.ops;

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

public class CodeGenerator {

  public static void main(String[] args) throws Exception {
    GeneratorConfig config = GeneratorConfig.load();
    System.out.println("Generating code for tables: " + config.tables());
    System.out.println("Output directory: " + config.outputDir());
    System.out.println("DB URL: " + config.url());
    System.out.println("DB username: " + config.username());
    System.out.println("DB password: [" + config.password() + "]");

    generateEntityAndMapper(config);
    generateDaoAndDaoImpl(config);

    System.out.println("Code generation completed.");
    System.out.println("Please review files in: " + config.outputDir());
  }

  // ===== Configuration =====

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

  // ===== Entity and Mapper Generation (MyBatis-Plus) =====

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
                    .mapper("mapper"))
        .strategyConfig(
            builder ->
                builder
                    .addInclude(config.tables())
                    .addTablePrefix("sys_", "t_", "t_bi_", "tb_")
                    .entityBuilder()
                    .enableTableFieldAnnotation()
                    .enableLombok()
                    .idType(IdType.INPUT)
                    .naming(NamingStrategy.underline_to_camel)
                    .columnNaming(NamingStrategy.underline_to_camel)
                    .addIgnoreColumns("created_at", "created_by", "updated_at", "updated_by", "deleted")
                    .convertFileName(entityName -> entityName + "Entity")
                    .javaTemplate("/templates/entity.java.vm")
                    .mapperBuilder()
                    .enableBaseResultMap()
                    .enableBaseColumnList()
                    .mapperTemplate("/templates/mapper.java.vm")
                    .serviceBuilder()
                    .disable()
                    .controllerBuilder()
                    .disable())
        .templateConfig(builder -> builder.disable(TemplateType.XML))
        .execute();
  }

  // ===== DAO and DAO Impl Generation (Custom) =====

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

  // ===== Table Metadata =====

  record TableInfo(String name, String comment, List<FieldInfo> fields) {}

  record FieldInfo(
      String columnName, String propertyName, String javaType, String comment, boolean primaryKey) {}

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

  // ===== Type and Name Conversion =====

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

  // ===== Template Rendering =====

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
}

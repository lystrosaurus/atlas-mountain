package io.github.lystrosaurus.atlasmountain.integration;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductionMigrationIntegrationTest {

  private final DataSource dataSource;

  @Autowired
  ProductionMigrationIntegrationTest(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @BeforeAll
  void migrateCommonSchema() {
    Flyway flyway =
        Flyway.configure()
            .cleanDisabled(false)
            .locations("classpath:db/migration")
            .dataSource(dataSource)
            .load();
    flyway.clean();
    flyway.migrate();
  }

  @Test
  void commonMigrationsDoNotCreateDevelopmentAccount() {
    Integer count =
        new JdbcTemplate(dataSource)
            .queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE id = 1 AND username = 'admin'", Integer.class);

    assertThat(count).isZero();
  }
}

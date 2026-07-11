package io.github.lystrosaurus.atlasmountain.integration;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
public class ProductionMigrationIntegrationTest {

  private final DataSource dataSource;

  @Autowired
  public ProductionMigrationIntegrationTest(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Test
  public void commonMigrationsDoNotCreateDevelopmentAccount() {
    Flyway flyway =
        Flyway.configure()
            .cleanDisabled(false)
            .locations("classpath:db/migration")
            .dataSource(dataSource)
            .load();
    flyway.clean();
    flyway.migrate();

    Integer count =
        new JdbcTemplate(dataSource)
            .queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE id = 1 AND username = 'admin'", Integer.class);

    assertThat(count).isZero();
  }
}

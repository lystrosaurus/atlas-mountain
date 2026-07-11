package io.github.lystrosaurus.atlasmountain.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class RuntimeConfigurationTest {

  @Test
  void applicationDoesNotForceLocalProfile() throws Exception {
    YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
    List<PropertySource<?>> propertySources =
        loader.load("application", new ClassPathResource("application.yml"));

    assertThat(propertySources)
        .isNotEmpty()
        .allSatisfy(
            propertySource ->
                assertThat(propertySource.getProperty("spring.profiles.active")).isNull());
  }

  @Test
  void buildExcludesLocalConfiguration() throws Exception {
    String pom = Files.readString(Path.of("pom.xml"));

    assertThat(pom).contains("<exclude>application-local.yml</exclude>");
  }

  @Test
  void developmentResourcesAreNotPackaged() {
    assertThat(new ClassPathResource("application-local.yml.example").exists()).isFalse();
    assertThat(new ClassPathResource("db/local/R__local_development_account.sql").exists())
        .isFalse();
  }
}

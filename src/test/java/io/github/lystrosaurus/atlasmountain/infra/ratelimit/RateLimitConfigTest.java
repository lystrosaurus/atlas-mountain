package io.github.lystrosaurus.atlasmountain.infra.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.github.lystrosaurus.atlasmountain.infra.ratelimit.config.RateLimitConfig;

class RateLimitConfigTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(RateLimitConfig.class);

  @Test
  void rateLimitingIsEnabledByDefault() {
    contextRunner.run(context -> assertThat(context).hasSingleBean(RateLimitAspect.class));
  }

  @Test
  void rateLimitingCanBeDisabled() {
    contextRunner
        .withPropertyValues("ratelimit.enabled=false")
        .run(context -> assertThat(context).doesNotHaveBean(RateLimitAspect.class));
  }
}

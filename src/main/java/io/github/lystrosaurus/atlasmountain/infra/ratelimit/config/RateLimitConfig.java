package io.github.lystrosaurus.atlasmountain.infra.ratelimit.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.lystrosaurus.atlasmountain.infra.ratelimit.RateLimitAspect;

@Configuration
public class RateLimitConfig {

  @Bean
  @ConditionalOnProperty(
      prefix = "ratelimit",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  public RateLimitAspect rateLimitAspect() {
    return new RateLimitAspect();
  }
}

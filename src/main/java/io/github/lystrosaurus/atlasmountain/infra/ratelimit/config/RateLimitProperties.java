package io.github.lystrosaurus.atlasmountain.infra.ratelimit.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "ratelimit")
@Data
public class RateLimitProperties {

  private boolean enabled = true;
  private Map<String, Limit> limits = new HashMap<>();

  @Data
  public static class Limit {
    private long capacity = 10;
    private Duration refillPeriod = Duration.ofMinutes(1);
    private long refillTokens = 10;
  }
}

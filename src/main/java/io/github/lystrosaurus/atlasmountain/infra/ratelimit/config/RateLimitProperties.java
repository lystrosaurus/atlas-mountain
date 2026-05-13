package io.github.lystrosaurus.atlasmountain.infra.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

  private boolean enabled = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}

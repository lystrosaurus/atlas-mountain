package io.github.lystrosaurus.atlasmountain.cdc.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "cdc")
@Getter
@Setter
public class CdcProperties {
  private boolean enabled = false;
  private String host = "localhost";
  private int port = 3306;
  private String username;
  private String password;
  private long serverId;
  private boolean keepAlive = true;
  private long keepAliveInterval = TimeUnit.MINUTES.toMillis(1L);
  private long connectTimeout = TimeUnit.SECONDS.toMillis(3L);
  private long heartbeatInterval = TimeUnit.SECONDS.toMillis(6L);

  // reconnect retry
  private int maxRetries = -1;
  private long initialRetryIntervalMs = TimeUnit.SECONDS.toMillis(1L);
  private long maxRetryIntervalMs = TimeUnit.MINUTES.toMillis(1L);
}

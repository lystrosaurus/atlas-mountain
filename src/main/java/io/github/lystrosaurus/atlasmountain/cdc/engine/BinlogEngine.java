package io.github.lystrosaurus.atlasmountain.cdc.engine;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

import io.github.lystrosaurus.atlasmountain.cdc.config.CdcProperties;
import io.github.lystrosaurus.atlasmountain.cdc.dispatcher.BinlogEventDispatcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinlogEngine implements Runnable {

  private static final double RETRY_MULTIPLIER = 2.0;

  private final CdcProperties cdcProperties;
  private final BinlogEventDispatcher dispatcher;
  private final AtomicBoolean running;

  private final AtomicReference<BinaryLogClient> clientRef = new AtomicReference<>();

  public BinlogEngine(
      CdcProperties cdcProperties, BinlogEventDispatcher dispatcher, AtomicBoolean running) {
    this.cdcProperties = cdcProperties;
    this.dispatcher = dispatcher;
    this.running = running;
  }

  @Override
  public void run() {
    log.info(
        "Starting binlog client (serverId={}) connecting to {}:{}",
        cdcProperties.getServerId(),
        cdcProperties.getHost(),
        cdcProperties.getPort());

    long retryIntervalMs = cdcProperties.getInitialRetryIntervalMs();
    int attempt = 0;

    while (running.get()) {
      attempt++;
      BinaryLogClient c = createClient();
      clientRef.set(c);

      boolean connected = false;
      try {
        c.connect();
        log.info("Binlog connection closed normally");
        retryIntervalMs = cdcProperties.getInitialRetryIntervalMs();
        attempt = 0;
        connected = true;
      } catch (IOException e) {
        if (running.get()) {
          log.warn(
              "Binlog connection failed, retrying in {}ms (attempt={})",
              retryIntervalMs,
              attempt,
              e);
        }
      }

      boolean exhausted =
          !connected
              && cdcProperties.getMaxRetries() > 0
              && attempt >= cdcProperties.getMaxRetries();
      if (exhausted) {
        log.error("Binlog reconnect exhausted after {} attempts, giving up", attempt);
        running.set(false);
      }

      if (!running.get() || exhausted) {
        break;
      }

      sleep(retryIntervalMs);
      retryIntervalMs =
          Math.min(
              (long) (retryIntervalMs * RETRY_MULTIPLIER), cdcProperties.getMaxRetryIntervalMs());
    }

    log.info("Binlog engine thread exiting");
  }

  public void close() {
    log.info("Shutting down binlog engine");
    running.set(false);
    BinaryLogClient c = clientRef.get();
    if (c != null) {
      try {
        c.disconnect();
      } catch (IOException e) {
        log.warn("Failed to close binlog client", e);
      }
    }
  }

  BinaryLogClient createClient() {
    BinaryLogClient c =
        new BinaryLogClient(
            cdcProperties.getHost(),
            cdcProperties.getPort(),
            cdcProperties.getUsername(),
            cdcProperties.getPassword());
    c.registerEventListener(dispatcher);
    c.setKeepAlive(cdcProperties.isKeepAlive());
    c.setKeepAliveInterval(cdcProperties.getKeepAliveInterval());
    c.setHeartbeatInterval(cdcProperties.getHeartbeatInterval());
    c.setConnectTimeout(cdcProperties.getConnectTimeout());
    c.setServerId(cdcProperties.getServerId());
    return c;
  }

  private void sleep(long millis) {
    try {
      TimeUnit.MILLISECONDS.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      running.set(false);
      log.info("Reconnect sleep interrupted, exiting");
    }
  }
}

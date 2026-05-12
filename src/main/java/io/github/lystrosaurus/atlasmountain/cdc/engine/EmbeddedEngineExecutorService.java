package io.github.lystrosaurus.atlasmountain.cdc.engine;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.SmartLifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmbeddedEngineExecutorService implements SmartLifecycle {

  private final BinlogEngine engine;
  private final ExecutorService executor;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public EmbeddedEngineExecutorService(BinlogEngine engine) {
    this.engine = engine;
    this.executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void start() {
    log.info("Starting embedded CDC engine");
    executor.execute(engine);
    running.set(true);
  }

  @Override
  public void stop() {
    log.info("Stopping embedded CDC engine");
    running.set(false);
    try {
      engine.close();
    } catch (IOException e) {
      log.warn("Failed to close binlog engine", e);
    }
    executor.shutdown();
    try {
      if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
        log.warn("CDC executor did not terminate gracefully, forcing shutdown");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }
}

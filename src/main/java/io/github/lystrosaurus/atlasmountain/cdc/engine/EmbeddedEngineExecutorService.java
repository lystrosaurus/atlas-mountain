package io.github.lystrosaurus.atlasmountain.cdc.engine;

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
  private final AtomicBoolean running;

  public EmbeddedEngineExecutorService(BinlogEngine engine, AtomicBoolean running) {
    this.engine = engine;
    this.running = running;
    this.executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void start() {
    log.info("Starting embedded CDC engine");
    running.set(true);
    executor.execute(engine);
  }

  @Override
  public void stop() {
    log.info("Stopping embedded CDC engine");
    running.set(false);
    engine.close();
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

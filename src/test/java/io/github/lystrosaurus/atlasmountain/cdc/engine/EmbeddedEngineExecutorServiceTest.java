package io.github.lystrosaurus.atlasmountain.cdc.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

import io.github.lystrosaurus.atlasmountain.cdc.config.CdcProperties;
import io.github.lystrosaurus.atlasmountain.cdc.dispatcher.BinlogEventDispatcher;
import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;

class EmbeddedEngineExecutorServiceTest {

  private CdcProperties properties;
  private AtomicBoolean running;
  private EmbeddedEngineExecutorService executorService;

  @BeforeEach
  void setUp() {
    properties = new CdcProperties();
    properties.setHost("localhost");
    properties.setPort(3306);
    properties.setUsername("test");
    properties.setPassword("test");
    properties.setServerId(9999L);
    properties.setInitialRetryIntervalMs(50L);
    properties.setMaxRetryIntervalMs(200L);
    running = new AtomicBoolean(false);
  }

  @AfterEach
  void tearDown() {
    if (executorService != null && executorService.isRunning()) {
      executorService.stop();
    }
  }

  @Test
  void startSetsRunningBeforeExecutingEngine() {
    BinlogEngine engine = createBlockingEngine();
    executorService = new EmbeddedEngineExecutorService(engine, running);

    assertThat(executorService.isRunning()).isFalse();

    executorService.start();

    assertThat(executorService.isRunning()).isTrue();
    assertThat(running.get()).isTrue();
  }

  @Test
  void stopClearsRunningAndShutsDown() {
    BinlogEngine engine = createBlockingEngine();
    executorService = new EmbeddedEngineExecutorService(engine, running);

    executorService.start();
    assertThat(executorService.isRunning()).isTrue();

    executorService.stop();
    assertThat(executorService.isRunning()).isFalse();
    assertThat(running.get()).isFalse();
  }

  @Test
  void stopIsIdempotent() {
    BinlogEngine engine = createBlockingEngine();
    executorService = new EmbeddedEngineExecutorService(engine, running);

    executorService.start();
    executorService.stop();
    executorService.stop();

    assertThat(executorService.isRunning()).isFalse();
  }

  @Test
  void isRunningReflectsSharedState() {
    BinlogEngine engine = createBlockingEngine();
    executorService = new EmbeddedEngineExecutorService(engine, running);

    executorService.start();
    assertThat(executorService.isRunning()).isTrue();

    running.set(false);
    // isRunning() should reflect the shared AtomicBoolean
    assertThat(executorService.isRunning()).isFalse();
  }

  @Test
  void stopTerminatesWithinTimeout() {
    BinlogEngine engine = createBlockingEngine();
    executorService = new EmbeddedEngineExecutorService(engine, running);

    executorService.start();

    long start = System.currentTimeMillis();
    executorService.stop();
    long elapsed = System.currentTimeMillis() - start;

    assertThat(executorService.isRunning()).isFalse();
    assertThat(elapsed).isLessThan(15_000);
  }

  private BinlogEngine createBlockingEngine() {
    BinlogEventDispatcher dispatcher =
        new BinlogEventDispatcher(List.of(mock(BinlogEventHandler.class)));
    return new BinlogEngine(properties, dispatcher, running) {
      @Override
      BinaryLogClient createClient() {
        BinaryLogClient client = mock(BinaryLogClient.class);
        try {
          doAnswer(
                  invocation -> {
                    try {
                      new CountDownLatch(1).await();
                    } catch (InterruptedException exception) {
                      Thread.currentThread().interrupt();
                      throw new IOException("binlog connection interrupted", exception);
                    }
                    return null;
                  })
              .when(client)
              .connect();
        } catch (Exception e) {
          // ignore - InterruptedException expected on close
        }
        return client;
      }
    };
  }
}

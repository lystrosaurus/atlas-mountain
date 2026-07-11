package io.github.lystrosaurus.atlasmountain.cdc.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

import io.github.lystrosaurus.atlasmountain.cdc.config.CdcProperties;
import io.github.lystrosaurus.atlasmountain.cdc.dispatcher.BinlogEventDispatcher;
import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;

class BinlogEngineTest {

  private CdcProperties properties;
  private AtomicBoolean running;

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
    running = new AtomicBoolean(true);
  }

  @AfterEach
  void tearDown() {
    running.set(false);
  }

  @Test
  void closeSetsRunningToFalse() throws Exception {
    BinlogEngine engine = createEngineWithBlockingClient();
    Thread thread = new Thread(engine);
    thread.start();

    await()
        .atMost(2, TimeUnit.SECONDS)
        .until(
            () ->
                !running.get()
                    || thread.getState() == Thread.State.WAITING
                    || thread.getState() == Thread.State.TIMED_WAITING);

    engine.close();
    thread.join(2000);
    assertThat(running.get()).isFalse();
  }

  @Test
  void retriesOnConnectionFailure() throws Exception {
    BinlogEngine engine = createEngineWithFailingClient(3);

    Thread thread = new Thread(engine);
    thread.start();

    // engine should exit after maxRetries=3 exhausted
    thread.join(3000);
    assertThat(thread.isAlive()).isFalse();
    assertThat(running.get()).isFalse();
  }

  @Test
  void stopsWhenRunningSetToFalse() throws Exception {
    properties.setMaxRetries(-1);
    AtomicInteger connectAttempts = new AtomicInteger();
    BinlogEngine engine =
        new BinlogEngine(properties, createDispatcher(), running) {
          @Override
          BinaryLogClient createClient() {
            BinaryLogClient client = mock(BinaryLogClient.class);
            connectAttempts.incrementAndGet();
            try {
              doThrow(new IOException("connection refused")).when(client).connect();
            } catch (IOException e) {
              // mock setup
            }
            return client;
          }
        };

    Thread thread = new Thread(engine);
    thread.start();

    await().atMost(2, TimeUnit.SECONDS).until(() -> connectAttempts.get() >= 2);

    running.set(false);

    thread.join(2000);
    assertThat(thread.isAlive()).isFalse();
  }

  @Test
  public void interruptedRetryStopsEngine() throws Exception {
    properties.setInitialRetryIntervalMs(TimeUnit.SECONDS.toMillis(10));
    BinlogEngine engine = createEngineWithFailingClient(-1);
    Thread thread = new Thread(engine);
    thread.start();

    try {
      await()
          .atMost(2, TimeUnit.SECONDS)
          .until(() -> thread.getState() == Thread.State.TIMED_WAITING);
      thread.interrupt();
      thread.join(1000);

      assertThat(thread.isAlive()).isFalse();
      assertThat(running.get()).isFalse();
    } finally {
      running.set(false);
      thread.interrupt();
      thread.join(2000);
    }
  }

  @Test
  void resetsAttemptCounterAfterSuccessfulConnection() throws Exception {
    properties.setMaxRetries(3);
    int[] connectCount = {0};

    BinlogEngine engine =
        new BinlogEngine(properties, createDispatcher(), running) {
          @Override
          BinaryLogClient createClient() {
            BinaryLogClient client = mock(BinaryLogClient.class);
            connectCount[0]++;
            try {
              if (connectCount[0] != 3) {
                doThrow(new IOException("fail " + connectCount[0])).when(client).connect();
              }
              // connectCount=3: no exception -> success
            } catch (IOException e) {
              // mock setup
            }
            return client;
          }
        };

    Thread thread = new Thread(engine);
    thread.start();

    // Without attempt reset: engine exits after 3 failures (attempt=3 >= maxRetries=3)
    // With attempt reset: engine survives because after success at connectCount=3, attempt resets
    // to
    // 0. So we should see more than 3 total connect attempts.
    thread.join(5000);
    assertThat(connectCount[0]).isGreaterThan(3);

    engine.close();
    thread.join(2000);
  }

  private BinlogEngine createEngineWithBlockingClient() {
    return new BinlogEngine(properties, createDispatcher(), running) {
      @Override
      BinaryLogClient createClient() {
        BinaryLogClient client = mock(BinaryLogClient.class);
        try {
          doAnswer(
                  invocation -> {
                    new CountDownLatch(1).await();
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

  private BinlogEngine createEngineWithFailingClient(int maxRetries) {
    properties.setMaxRetries(maxRetries);
    return new BinlogEngine(properties, createDispatcher(), running) {
      @Override
      BinaryLogClient createClient() {
        BinaryLogClient client = mock(BinaryLogClient.class);
        try {
          doThrow(new IOException("connection refused")).when(client).connect();
        } catch (IOException e) {
          // mock setup
        }
        return client;
      }
    };
  }

  private BinlogEventDispatcher createDispatcher() {
    return new BinlogEventDispatcher(List.of(mock(BinlogEventHandler.class)));
  }
}

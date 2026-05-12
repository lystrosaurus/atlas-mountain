package io.github.lystrosaurus.atlasmountain.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.infra.redis.DistributedLockService;

class DistributedLockIntegrationTest extends IntegrationTestBase {

  @Autowired private DistributedLockService lockService;

  @Autowired private RedissonClient redissonClient;

  private final List<String> keysToClean = new ArrayList<>();

  @AfterEach
  void cleanup() {
    for (String key : keysToClean) {
      redissonClient.getKeys().delete(key);
    }
    keysToClean.clear();
  }

  @Test
  void singleThreadAcquiresLockSuccessfully() {
    keysToClean.add("test-resource");

    String result = lockService.execute("test-resource", 1, 5, TimeUnit.SECONDS, () -> "done");

    assertThat(result).isEqualTo("done");
  }

  @Test
  void concurrentAccessOneSucceedsOneFails() throws InterruptedException {
    String key = "concurrent-resource";
    keysToClean.add(key);

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch lockAcquiredLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(2);

    AtomicReference<String> successResult = new AtomicReference<>();
    AtomicReference<Exception> failException = new AtomicReference<>();

    ExecutorService executor = Executors.newFixedThreadPool(2);

    executor.submit(
        () -> {
          try {
            startLatch.await();
            String result =
                lockService.execute(
                    key,
                    1,
                    10,
                    TimeUnit.SECONDS,
                    () -> {
                      lockAcquiredLatch.countDown();
                      await().during(Duration.ofSeconds(3)).until(() -> true);
                      return "A-done";
                    });
            successResult.set(result);
          } catch (Exception e) {
            // ignore
          } finally {
            endLatch.countDown();
          }
        });

    executor.submit(
        () -> {
          try {
            startLatch.await();
            lockAcquiredLatch.await();
            lockService.execute(key, 1, 10, TimeUnit.SECONDS, () -> "B-done");
          } catch (Exception e) {
            failException.set(e);
          } finally {
            endLatch.countDown();
          }
        });

    startLatch.countDown();
    endLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(successResult.get()).isEqualTo("A-done");
    assertThat(failException.get())
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> {
              BusinessException be = (BusinessException) ex;
              assertThat(be.errorCode()).isEqualTo(CommonErrorCode.LOCK_BUSY);
            });
  }

  @Test
  void lockAutoReleasesAfterLeaseTime() {
    String key = "auto-release-key";
    keysToClean.add(key);

    String result1 = lockService.execute(key, 1, 2, TimeUnit.SECONDS, () -> "first");

    AtomicReference<String> result2Holder = new AtomicReference<>();
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(200))
        .until(
            () -> {
              result2Holder.set(lockService.execute(key, 1, 2, TimeUnit.SECONDS, () -> "second"));
              return true;
            });

    assertThat(result1).isEqualTo("first");
    assertThat(result2Holder.get()).isEqualTo("second");
  }
}

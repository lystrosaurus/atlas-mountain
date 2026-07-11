package io.github.lystrosaurus.atlasmountain.infra.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;

class RedissonDistributedLockServiceTest {

  @AfterEach
  public void clearInterruptedStatus() {
    Thread.interrupted();
  }

  @Test
  void executesActionWhenLockAcquired() throws Exception {
    RedissonClient redissonClient = mock(RedissonClient.class);
    RLock lock = mock(RLock.class);
    when(redissonClient.getLock("test-key")).thenReturn(lock);
    when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenReturn(true);
    when(lock.isHeldByCurrentThread()).thenReturn(true);

    RedissonDistributedLockService service = new RedissonDistributedLockService(redissonClient);
    String result = service.execute("test-key", 1, 5, TimeUnit.SECONDS, () -> "done");

    assertThat(result).isEqualTo("done");
    verify(lock).unlock();
  }

  @Test
  public void doesNotUnlockWhenLeaseIsNoLongerOwned() throws Exception {
    RedissonClient redissonClient = mock(RedissonClient.class);
    RLock lock = mock(RLock.class);
    when(redissonClient.getLock("expired-key")).thenReturn(lock);
    when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenReturn(true);
    when(lock.isHeldByCurrentThread()).thenReturn(false);

    RedissonDistributedLockService service = new RedissonDistributedLockService(redissonClient);
    String result = service.execute("expired-key", 1, 5, TimeUnit.SECONDS, () -> "done");

    assertThat(result).isEqualTo("done");
    verify(lock, never()).unlock();
  }

  @Test
  void throwsBusinessExceptionWhenLockNotAcquired() throws Exception {
    RedissonClient redissonClient = mock(RedissonClient.class);
    RLock lock = mock(RLock.class);
    when(redissonClient.getLock("busy-key")).thenReturn(lock);
    when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenReturn(false);

    RedissonDistributedLockService service = new RedissonDistributedLockService(redissonClient);

    assertThatThrownBy(() -> service.execute("busy-key", 1, 5, TimeUnit.SECONDS, () -> "done"))
        .isInstanceOf(BusinessException.class)
        .extracting(ex -> ((BusinessException) ex).errorCode())
        .isEqualTo(CommonErrorCode.LOCK_BUSY);
  }

  @Test
  void throwsBusinessExceptionWhenInterrupted() throws Exception {
    RedissonClient redissonClient = mock(RedissonClient.class);
    RLock lock = mock(RLock.class);
    when(redissonClient.getLock("interrupt-key")).thenReturn(lock);
    when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

    RedissonDistributedLockService service = new RedissonDistributedLockService(redissonClient);

    assertThatThrownBy(() -> service.execute("interrupt-key", 1, 5, TimeUnit.SECONDS, () -> "done"))
        .isInstanceOf(BusinessException.class)
        .extracting(ex -> ((BusinessException) ex).errorCode())
        .isEqualTo(CommonErrorCode.LOCK_BUSY);
    assertThat(Thread.currentThread().isInterrupted()).isTrue();
  }
}

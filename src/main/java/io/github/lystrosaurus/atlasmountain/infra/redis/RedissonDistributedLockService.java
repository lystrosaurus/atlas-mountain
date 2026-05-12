package io.github.lystrosaurus.atlasmountain.infra.redis;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;

@Service
public class RedissonDistributedLockService implements DistributedLockService {

  private final RedissonClient redissonClient;

  public RedissonDistributedLockService(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  @Override
  public <T> T execute(
      String key, long waitTime, long leaseTime, TimeUnit timeUnit, Callable<T> action) {
    RLock lock = redissonClient.getLock(key);
    boolean locked = false;
    try {
      locked = lock.tryLock(waitTime, leaseTime, timeUnit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BusinessException(CommonErrorCode.LOCK_BUSY);
    }
    if (!locked) {
      throw new BusinessException(CommonErrorCode.LOCK_BUSY);
    }
    try {
      return action.call();
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      lock.unlock();
    }
  }
}

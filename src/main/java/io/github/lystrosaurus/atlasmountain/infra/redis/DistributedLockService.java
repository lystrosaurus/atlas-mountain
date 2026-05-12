package io.github.lystrosaurus.atlasmountain.infra.redis;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface DistributedLockService {

  <T> T execute(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Callable<T> action);
}

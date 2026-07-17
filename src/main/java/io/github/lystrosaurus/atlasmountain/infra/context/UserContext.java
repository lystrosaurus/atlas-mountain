package io.github.lystrosaurus.atlasmountain.infra.context;

/**
 * Holds the current request's user id in a thread-local, so that lower layers (e.g. MyBatis-Plus
 * {@link com.baomidou.mybatisplus.core.handlers.MetaObjectHandler}) can populate audit columns
 * without depending on the web/session stack.
 *
 * <p>Populated by a web-layer interceptor on request start and cleared on completion. Threads
 * without a bound user (CDC, async tasks, scheduled jobs) fall back to {@link #SYSTEM_USER_ID}.
 */
public final class UserContext {

  private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
  private static final Long SYSTEM_USER_ID = 0L;

  private UserContext() {}

  public static void setCurrentUserId(Long userId) {
    if (userId != null) {
      CURRENT_USER_ID.set(userId);
    }
  }

  public static Long getCurrentUserId() {
    Long id = CURRENT_USER_ID.get();
    return id != null ? id : SYSTEM_USER_ID;
  }

  public static void clear() {
    CURRENT_USER_ID.remove();
  }
}

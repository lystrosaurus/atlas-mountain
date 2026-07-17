package io.github.lystrosaurus.atlasmountain.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.infra.context.UserContext;

/**
 * Binds the current Sa-Token login id into {@link UserContext} so that DAO-layer audit fillers can
 * read it without a direct dependency on the session/web stack.
 *
 * <p>Must run after the Sa-Token interceptor so that authentication is already resolved.
 * Unauthenticated routes (public / login / open-api) keep the default {@code SYSTEM_USER_ID}.
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    try {
      UserContext.setCurrentUserId(StpUtil.getLoginIdAsLong());
    } catch (NotLoginException ignored) {
      // Unauthenticated route — keep SYSTEM_USER_ID.
    }
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    UserContext.clear();
  }
}

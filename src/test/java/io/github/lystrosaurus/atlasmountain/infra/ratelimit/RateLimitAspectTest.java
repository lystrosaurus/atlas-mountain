package io.github.lystrosaurus.atlasmountain.infra.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;

class RateLimitAspectTest {

  private final RateLimitAspect aspect = new RateLimitAspect();

  @Test
  void shouldAllowRequestsWithinCapacity() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("TestClass");
    when(signature.getName()).thenReturn("testMethod");
    when(signature.getParameterNames()).thenReturn(new String[0]);
    when(joinPoint.proceed()).thenReturn("success");

    RateLimit rateLimit = createRateLimit(2, 60, 2);

    assertThat(aspect.around(joinPoint, rateLimit)).isEqualTo("success");
    assertThat(aspect.around(joinPoint, rateLimit)).isEqualTo("success");
  }

  @Test
  void shouldBlockRequestsExceedingCapacity() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("TestClass");
    when(signature.getName()).thenReturn("testMethod");
    when(signature.getParameterNames()).thenReturn(new String[0]);
    when(joinPoint.proceed()).thenReturn("success");

    RateLimit rateLimit = createRateLimit(1, 60, 1);

    aspect.around(joinPoint, rateLimit);

    assertThatThrownBy(() -> aspect.around(joinPoint, rateLimit))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("too many requests");
  }

  @Test
  void shouldResolveSpelKeyWithMethodParameters() throws Throwable {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("TestClass");
    when(signature.getName()).thenReturn("testMethod");
    when(signature.getParameterNames()).thenReturn(new String[] {"request"});
    when(joinPoint.getArgs()).thenReturn(new Object[] {"alice"});
    when(joinPoint.proceed()).thenReturn("success");

    RateLimit rateLimit = createRateLimit("'login:' + #request", 1, 60, 1);

    assertThat(aspect.around(joinPoint, rateLimit)).isEqualTo("success");
    assertThatThrownBy(() -> aspect.around(joinPoint, rateLimit))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("too many requests");
  }

  private RateLimit createRateLimit(long capacity, long period, long tokens) {
    return createRateLimit("", capacity, period, tokens);
  }

  private RateLimit createRateLimit(String key, long capacity, long period, long tokens) {
    return new RateLimit() {
      @Override
      public Class<RateLimit> annotationType() {
        return RateLimit.class;
      }

      @Override
      public String key() {
        return key;
      }

      @Override
      public long capacity() {
        return capacity;
      }

      @Override
      public long refillPeriodSeconds() {
        return period;
      }

      @Override
      public long refillTokens() {
        return tokens;
      }
    };
  }
}

package io.github.lystrosaurus.atlasmountain.infra.ratelimit;

import java.time.Duration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;

@Aspect
@Component
public class RateLimitAspect {

  private static final int BUCKET_CACHE_MAX_SIZE = 1000;

  private final Cache<String, Bucket> buckets =
      Caffeine.newBuilder()
          .maximumSize(BUCKET_CACHE_MAX_SIZE)
          .expireAfterAccess(Duration.ofHours(1))
          .build();
  private final SpelExpressionParser parser = new SpelExpressionParser();

  @Around("@annotation(rateLimit)")
  public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
    String key = resolveKey(joinPoint, rateLimit.key());
    Bucket bucket =
        buckets.get(
            key,
            k -> {
              Bandwidth bandwidth =
                  Bandwidth.classic(
                      rateLimit.capacity(),
                      Refill.intervally(
                          rateLimit.refillTokens(),
                          Duration.ofSeconds(rateLimit.refillPeriodSeconds())));
              return Bucket.builder().addLimit(bandwidth).build();
            });

    if (!bucket.tryConsume(1)) {
      throw new BusinessException(CommonErrorCode.TOO_MANY_REQUESTS);
    }

    return joinPoint.proceed();
  }

  private String resolveKey(ProceedingJoinPoint joinPoint, String spelKey) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    if (spelKey == null || spelKey.isBlank()) {
      return signature.getDeclaringTypeName() + "." + signature.getName();
    }
    String[] parameterNames = signature.getParameterNames();
    if (parameterNames == null) {
      return signature.getDeclaringTypeName() + "." + signature.getName() + ":" + spelKey;
    }
    StandardEvaluationContext context = new StandardEvaluationContext();
    Object[] args = joinPoint.getArgs();
    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }
    return parser.parseExpression(spelKey).getValue(context, String.class);
  }
}

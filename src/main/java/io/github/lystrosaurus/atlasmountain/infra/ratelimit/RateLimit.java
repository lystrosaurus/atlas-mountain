package io.github.lystrosaurus.atlasmountain.infra.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

  String key() default "";

  long capacity() default 10;

  long refillPeriodSeconds() default 60;

  long refillTokens() default 10;
}

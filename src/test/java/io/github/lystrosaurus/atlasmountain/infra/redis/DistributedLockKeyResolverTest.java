package io.github.lystrosaurus.atlasmountain.infra.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.support.StandardEvaluationContext;

class DistributedLockKeyResolverTest {

  @Test
  void resolvesTemplateWithSpelVariable() {
    DistributedLockKeyResolver resolver = new DistributedLockKeyResolver();
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariable("resourceId", "42");

    String key = resolver.resolve("lock:#{#resourceId}", context);

    assertThat(key).isEqualTo("lock:42");
  }
}

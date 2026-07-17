package io.github.lystrosaurus.atlasmountain.infra.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UserContextTest {

  @AfterEach
  void clear() {
    UserContext.clear();
  }

  @Test
  void shouldReturnSystemUserIdWhenNothingBound() {
    assertThat(UserContext.getCurrentUserId()).isEqualTo(0L);
  }

  @Test
  void shouldReturnBoundUserId() {
    UserContext.setCurrentUserId(42L);

    assertThat(UserContext.getCurrentUserId()).isEqualTo(42L);
  }

  @Test
  void shouldIgnoreNullUserId() {
    UserContext.setCurrentUserId(42L);
    UserContext.setCurrentUserId(null);

    assertThat(UserContext.getCurrentUserId()).isEqualTo(42L);
  }

  @Test
  void shouldClearBoundUserId() {
    UserContext.setCurrentUserId(42L);
    UserContext.clear();

    assertThat(UserContext.getCurrentUserId()).isEqualTo(0L);
  }
}

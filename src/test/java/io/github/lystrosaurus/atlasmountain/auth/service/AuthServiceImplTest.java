package io.github.lystrosaurus.atlasmountain.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;

class AuthServiceImplTest {

  @Test
  void loginWithWrongPasswordThrowsUnauthorized() {
    UserService userService = mock(UserService.class);
    UserEntity user = new UserEntity();
    user.setUsername("admin");
    user.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqhmM6JGKpS4G3R1G2JH8YpfB0Bqy");
    user.setStatus("ENABLED");
    when(userService.findLoginUser("admin")).thenReturn(Optional.of(user));

    AuthService authService = new AuthServiceImpl(userService);
    LoginRequest request = new LoginRequest("admin", "wrong-password");

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("unauthorized");
  }

  @Test
  void loginWithUnknownUserThrowsUnauthorized() {
    UserService userService = mock(UserService.class);
    when(userService.findLoginUser("unknown")).thenReturn(Optional.empty());

    AuthService authService = new AuthServiceImpl(userService);
    LoginRequest request = new LoginRequest("unknown", "any-password");

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("unauthorized");
  }
}

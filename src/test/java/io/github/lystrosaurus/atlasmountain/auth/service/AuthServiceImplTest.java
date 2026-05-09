package io.github.lystrosaurus.atlasmountain.auth.service;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("unauthorized");
    }

    @Test
    void loginWithUnknownUserThrowsUnauthorized() {
        UserService userService = mock(UserService.class);
        when(userService.findLoginUser("unknown")).thenReturn(Optional.empty());

        AuthService authService = new AuthServiceImpl(userService);

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "any-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("unauthorized");
    }
}

package io.github.lystrosaurus.atlasmountain.user.mapstruct;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;

class UserMapstructMapperTest {

  private final UserMapstructMapper mapper = new UserMapstructMapperImpl();

  @Test
  void shouldMapUserEntityToCurrentUserVo() {
    UserEntity entity = new UserEntity();
    entity.setId(1L);
    entity.setUsername("alice");
    entity.setNickname("Alice");
    entity.setPasswordHash("should-not-appear");
    entity.setStatus(UserEntity.STATUS_ENABLED);

    CurrentUserVo vo = mapper.toCurrentUserVo(entity);

    assertThat(vo.id()).isEqualTo(1L);
    assertThat(vo.username()).isEqualTo("alice");
    assertThat(vo.nickname()).isEqualTo("Alice");
  }
}

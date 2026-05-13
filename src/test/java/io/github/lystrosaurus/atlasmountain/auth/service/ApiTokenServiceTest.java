package io.github.lystrosaurus.atlasmountain.auth.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.lystrosaurus.atlasmountain.auth.dao.ApiTokenDao;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;

class ApiTokenServiceTest {

  @Test
  void validTokenPasses() {
    ApiTokenDao dao = mock(ApiTokenDao.class);
    ApiTokenEntity entity = new ApiTokenEntity();
    entity.setTokenPrefix("demo");
    entity.setTokenHash("2392d485a4e225fc68efdbf5bbcc5ee9dc1334da141e9aba791f26cc8b23f525");
    entity.setStatus(ApiTokenEntity.STATUS_ENABLED);
    entity.setExpiresAt(LocalDateTime.now().plusDays(1));
    when(dao.findByPrefix("demo")).thenReturn(Optional.of(entity));

    ApiTokenService service = new ApiTokenService(dao);

    assertThatCode(() -> service.verify("ak_demo_secret")).doesNotThrowAnyException();
  }

  @Test
  void malformedTokenFails() {
    ApiTokenService service = new ApiTokenService(mock(ApiTokenDao.class));

    assertThatThrownBy(() -> service.verify("invalid")).isInstanceOf(BusinessException.class);
  }
}

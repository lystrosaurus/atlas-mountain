package io.github.lystrosaurus.atlasmountain.web.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;

class GlobalExceptionHandlerTest {

  @Test
  void businessExceptionMapsToErrorResponse() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleBusinessException(new BusinessException(CommonErrorCode.UNAUTHORIZED));

    assertThat(response.getStatusCode().value()).isEqualTo(401);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("COMMON_401");
  }
}

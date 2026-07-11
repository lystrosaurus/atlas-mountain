package io.github.lystrosaurus.atlasmountain.web.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;

@ExtendWith(OutputCaptureExtension.class)
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

  @Test
  public void internalBusinessExceptionMapsToServerError() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleBusinessException(new BusinessException(CommonErrorCode.INTERNAL_ERROR));

    assertThat(response.getStatusCode().value()).isEqualTo(500);
  }

  @Test
  public void unexpectedExceptionIsLogged(CapturedOutput output) {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    handler.handleException(new IllegalStateException("unexpected failure"));

    assertThat(output).contains("Unhandled exception").contains("unexpected failure");
  }
}

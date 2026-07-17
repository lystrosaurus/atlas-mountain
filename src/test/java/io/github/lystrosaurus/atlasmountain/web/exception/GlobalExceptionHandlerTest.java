package io.github.lystrosaurus.atlasmountain.web.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
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
  void internalBusinessExceptionMapsToServerError() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleBusinessException(new BusinessException(CommonErrorCode.INTERNAL_ERROR));

    assertThat(response.getStatusCode().value()).isEqualTo(500);
  }

  @Test
  void conflictBusinessExceptionMapsToConflictStatus() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleBusinessException(new BusinessException(CommonErrorCode.CONFLICT));

    assertThat(response.getStatusCode().value()).isEqualTo(409);
  }

  @Test
  void tooManyRequestsBusinessExceptionMapsTo429() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleBusinessException(new BusinessException(CommonErrorCode.TOO_MANY_REQUESTS));

    assertThat(response.getStatusCode().value()).isEqualTo(429);
  }

  @Test
  void notRoleExceptionMapsToForbidden() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleAccessDeniedException(new NotRoleException("admin"));

    assertThat(response.getStatusCode().value()).isEqualTo(403);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("COMMON_403");
  }

  @Test
  void notPermissionExceptionMapsToForbidden() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleAccessDeniedException(new NotPermissionException("user:read"));

    assertThat(response.getStatusCode().value()).isEqualTo(403);
    assertThat(response.getBody().code()).isEqualTo("COMMON_403");
  }

  @Test
  void disableServiceExceptionMapsToForbidden() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    ResponseEntity<ApiResponse<Void>> response =
        handler.handleDisableServiceException(mock(DisableServiceException.class));

    assertThat(response.getStatusCode().value()).isEqualTo(403);
    assertThat(response.getBody().code()).isEqualTo("COMMON_403");
  }

  @Test
  void unexpectedExceptionIsLogged(CapturedOutput output) {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    handler.handleException(new IllegalStateException("unexpected failure"));

    assertThat(output).contains("Unhandled exception").contains("unexpected failure");
  }
}

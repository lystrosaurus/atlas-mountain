package io.github.lystrosaurus.atlasmountain.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.dev33.satoken.exception.NotLoginException;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
    HttpStatus status =
        switch (exception.errorCode().code()) {
          case "COMMON_401" -> HttpStatus.UNAUTHORIZED;
          case "COMMON_403" -> HttpStatus.FORBIDDEN;
          case "COMMON_404" -> HttpStatus.NOT_FOUND;
          case "COMMON_409", "LOCK_409" -> HttpStatus.CONFLICT;
          default -> HttpStatus.BAD_REQUEST;
        };
    return ResponseEntity.status(status)
        .body(ApiResponse.failure(exception.errorCode().code(), exception.getMessage()));
  }

  @ExceptionHandler(NotLoginException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotLoginException(NotLoginException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            ApiResponse.failure(
                CommonErrorCode.UNAUTHORIZED.code(), CommonErrorCode.UNAUTHORIZED.message()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException exception) {
    return ResponseEntity.badRequest()
        .body(ApiResponse.failure(CommonErrorCode.BAD_REQUEST.code(), "validation failed"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
    return ResponseEntity.internalServerError()
        .body(
            ApiResponse.failure(
                CommonErrorCode.INTERNAL_ERROR.code(), CommonErrorCode.INTERNAL_ERROR.message()));
  }
}

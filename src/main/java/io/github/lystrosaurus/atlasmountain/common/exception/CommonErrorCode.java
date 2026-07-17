package io.github.lystrosaurus.atlasmountain.common.exception;

public enum CommonErrorCode implements ErrorCode {
  BAD_REQUEST("COMMON_400", "bad request", 400),
  UNAUTHORIZED("COMMON_401", "unauthorized", 401),
  FORBIDDEN("COMMON_403", "forbidden", 403),
  NOT_FOUND("COMMON_404", "not found", 404),
  CONFLICT("COMMON_409", "conflict", 409),
  TOO_MANY_REQUESTS("COMMON_429", "too many requests", 429),
  INTERNAL_ERROR("COMMON_500", "internal server error", 500),
  LOCK_BUSY("LOCK_409", "resource is busy", 409);

  private final String code;
  private final String message;
  private final int httpStatus;

  CommonErrorCode(String code, String message, int httpStatus) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  @Override
  public String code() {
    return code;
  }

  @Override
  public String message() {
    return message;
  }

  @Override
  public int httpStatus() {
    return httpStatus;
  }
}

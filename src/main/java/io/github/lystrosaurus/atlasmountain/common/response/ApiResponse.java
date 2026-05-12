package io.github.lystrosaurus.atlasmountain.common.response;

public record ApiResponse<T>(String code, String message, T data) {

  private static final String SUCCESS_CODE = "0";
  private static final String SUCCESS_MESSAGE = "success";

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
  }

  public static ApiResponse<Void> success() {
    return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
  }

  public static <T> ApiResponse<T> failure(String code, String message) {
    return new ApiResponse<>(code, message, null);
  }
}

package io.github.lystrosaurus.atlasmountain.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

  @Test
  void successWrapsDataWithSuccessCode() {
    ApiResponse<String> response = ApiResponse.success("ok");

    assertThat(response.code()).isEqualTo("0");
    assertThat(response.message()).isEqualTo("success");
    assertThat(response.data()).isEqualTo("ok");
  }

  @Test
  void failureWrapsErrorCodeAndMessage() {
    ApiResponse<Void> response = ApiResponse.failure("AUTH_401", "login required");

    assertThat(response.code()).isEqualTo("AUTH_401");
    assertThat(response.message()).isEqualTo("login required");
    assertThat(response.data()).isNull();
  }
}

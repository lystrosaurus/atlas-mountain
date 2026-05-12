package io.github.lystrosaurus.atlasmountain.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import cn.dev33.satoken.stp.StpUtil;

class PingIntegrationTest extends MockMvcIntegrationTest {

  @AfterEach
  void cleanup() {
    jdbcTemplate.update("DELETE FROM api_token WHERE id = 9991");
  }

  @Test
  void publicPingReturnsSuccess() throws Exception {
    setSaTokenContext("/api/public/ping", "GET");
    mockMvc
        .perform(get("/api/public/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.message").value("success"));
  }

  @Test
  void openPingWithoutTokenReturnsUnauthorized() throws Exception {
    setSaTokenContext("/api/open/ping", "GET");
    mockMvc
        .perform(get("/api/open/ping"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("COMMON_401"));
  }

  @Test
  void openPingWithValidTokenReturnsSuccess() throws Exception {
    String token = "ak_test_validtoken";
    String hash = sha256(token);
    jdbcTemplate.update(
        "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at,"
            + " created_at, created_by, updated_at, updated_by, deleted) VALUES (9991,"
            + " 'Ping Test', 'test', ?, 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
        hash,
        LocalDateTime.now().plusDays(1));

    setSaTokenContext("/api/open/ping", "GET", "X-API-Token", token);
    mockMvc
        .perform(get("/api/open/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void appPingWithoutSessionReturnsUnauthorized() throws Exception {
    setSaTokenContext("/api/app/ping", "GET");
    mockMvc
        .perform(get("/api/app/ping"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("COMMON_401"));
  }

  @Test
  void appPingWithSessionReturnsSuccess() throws Exception {
    setSaTokenContext("/api/auth/login", "POST");
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"atlas-local\"}"))
        .andExpect(status().isOk());

    String token = StpUtil.getTokenValue();
    assertNotNull(token, "satoken not found after login");

    setSaTokenContext("/api/app/ping", "GET", "satoken", token);
    mockMvc
        .perform(get("/api/app/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.message").value("success"));
  }
}

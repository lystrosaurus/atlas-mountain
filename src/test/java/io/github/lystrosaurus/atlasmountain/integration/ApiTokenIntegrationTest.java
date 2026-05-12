package io.github.lystrosaurus.atlasmountain.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ApiTokenIntegrationTest extends MockMvcIntegrationTest {

  @AfterEach
  void cleanup() {
    jdbcTemplate.update("DELETE FROM api_token WHERE id IN (9992, 9993, 9994)");
  }

  @Test
  void malformedTokenReturnsUnauthorized() throws Exception {
    setSaTokenContext("/api/open/ping", "GET", "X-API-Token", "invalid");
    mockMvc
        .perform(get("/api/open/ping"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("COMMON_401"));
  }

  @Test
  void prefixNotFoundReturnsUnauthorized() throws Exception {
    setSaTokenContext("/api/open/ping", "GET", "X-API-Token", "ak_notexist_secret");
    mockMvc
        .perform(get("/api/open/ping"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("COMMON_401"));
  }

  @Test
  void hashMismatchReturnsUnauthorized() throws Exception {
    jdbcTemplate.update(
        "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at,"
            + " created_at, created_by, updated_at, updated_by, deleted) VALUES (9992,"
            + " 'Mismatch Test', 'mismatch',"
            + " '0000000000000000000000000000000000000000000000000000000000000000',"
            + " 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
        LocalDateTime.now().plusDays(1));

    setSaTokenContext("/api/open/ping", "GET", "X-API-Token", "ak_mismatch_secret");
    mockMvc
        .perform(get("/api/open/ping"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("COMMON_401"));
  }

  @Test
  void expiredTokenReturnsUnauthorized() throws Exception {
    String token = "ak_expired_test";
    String hash = sha256(token);
    jdbcTemplate.update(
        "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at,"
            + " created_at, created_by, updated_at, updated_by, deleted) VALUES (9993,"
            + " 'Expired Test', 'expired', ?, 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
        hash,
        LocalDateTime.now().minusDays(1));

    setSaTokenContext("/api/open/ping", "GET", "X-API-Token", token);
    mockMvc
        .perform(get("/api/open/ping"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("COMMON_401"));
  }

  @Test
  void validTokenReturnsSuccess() throws Exception {
    String token = "ak_valid_test";
    String hash = sha256(token);
    jdbcTemplate.update(
        "INSERT INTO api_token (id, name, token_prefix, token_hash, status, expires_at,"
            + " created_at, created_by, updated_at, updated_by, deleted) VALUES (9994,"
            + " 'Valid Test', 'valid', ?, 'ENABLED', ?, NOW(), 1, NOW(), 1, 0)",
        hash,
        LocalDateTime.now().plusDays(1));

    setSaTokenContext("/api/open/ping", "GET", "X-API-Token", token);
    mockMvc
        .perform(get("/api/open/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }
}

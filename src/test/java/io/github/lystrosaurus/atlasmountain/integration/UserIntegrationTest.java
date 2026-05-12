package io.github.lystrosaurus.atlasmountain.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import cn.dev33.satoken.stp.StpUtil;

class UserIntegrationTest extends MockMvcIntegrationTest {

  @Test
  void getCurrentUserWithoutLoginReturnsUnauthorized() throws Exception {
    setSaTokenContext("/api/app/me", "GET");
    mockMvc
        .perform(get("/api/app/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("COMMON_401"));
  }

  @Test
  void getCurrentUserAfterLoginReturnsUserData() throws Exception {
    setSaTokenContext("/api/auth/login", "POST");
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"atlas-local\"}"))
        .andExpect(status().isOk());

    String token = StpUtil.getTokenValue();

    setSaTokenContext("/api/app/me", "GET", "satoken", token);
    mockMvc
        .perform(get("/api/app/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.username").value("admin"));
  }
}

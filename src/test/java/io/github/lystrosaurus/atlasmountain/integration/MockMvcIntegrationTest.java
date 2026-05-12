package io.github.lystrosaurus.atlasmountain.integration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaRequestForMock;
import cn.dev33.satoken.context.mock.SaResponseForMock;
import cn.dev33.satoken.context.mock.SaStorageForMock;

public abstract class MockMvcIntegrationTest extends IntegrationTestBase {
  protected MockMvc mockMvc;
  @Autowired protected JdbcTemplate jdbcTemplate;
  @Autowired private WebApplicationContext context;
  private final ThreadLocal<SaStorageForMock> storageHolder = new ThreadLocal<>();

  @BeforeEach
  void setupMockMvc() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @AfterEach
  void clearSaTokenContext() {
    storageHolder.remove();
    SaManager.getSaTokenContext().clearContext();
  }

  protected void setSaTokenContext(String path, String method, String... headers) {
    SaStorageForMock storage = storageHolder.get();
    if (storage == null) {
      storage = new SaStorageForMock();
      storageHolder.set(storage);
    }
    SaRequestForMock request = new SaRequestForMock();
    request.requestPath = path;
    request.url = "http://localhost" + path;
    request.method = method;
    for (int i = 0; i < headers.length; i += 2) {
      request.headerMap.put(headers[i], headers[i + 1]);
    }
    SaResponseForMock response = new SaResponseForMock();
    SaManager.getSaTokenContext().setContext(request, response, storage);
  }

  protected String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}

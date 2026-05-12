package io.github.lystrosaurus.atlasmountain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import io.github.lystrosaurus.atlasmountain.auth.dao.ApiTokenDao;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;

@Service
public class ApiTokenServiceImpl implements ApiTokenService {

  private final ApiTokenDao apiTokenDao;

  public ApiTokenServiceImpl(ApiTokenDao apiTokenDao) {
    this.apiTokenDao = apiTokenDao;
  }

  @Override
  public void verify(String token) {
    String prefix = extractPrefix(token);
    ApiTokenEntity apiToken =
        apiTokenDao
            .findByPrefix(prefix)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
    if (!"ENABLED".equals(apiToken.getStatus())) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    if (apiToken.getExpiresAt() != null && apiToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    if (!sha256(token).equals(apiToken.getTokenHash())) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
  }

  private String extractPrefix(String token) {
    if (token == null || !token.startsWith("ak_")) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    String[] parts = token.split("_", 3);
    if (parts.length != 3 || parts[1].isBlank() || parts[2].isBlank()) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    return parts[1];
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}

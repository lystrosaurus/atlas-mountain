package io.github.lystrosaurus.atlasmountain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import io.github.lystrosaurus.atlasmountain.auth.dao.ApiTokenDao;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

  private static final String TOKEN_PREFIX = "ak_";
  private static final int TOKEN_PART_COUNT = 3;

  private final ApiTokenDao apiTokenDao;

  public void verify(String token) {
    String prefix = extractPrefix(token);
    ApiTokenEntity apiToken =
        apiTokenDao
            .findByPrefix(prefix)
            .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
    if (!ApiTokenEntity.STATUS_ENABLED.equals(apiToken.getStatus())) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    if (apiToken.getExpiresAt() != null
        && apiToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneId.systemDefault()))) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    if (!sha256(token).equals(apiToken.getTokenHash())) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
  }

  private String extractPrefix(String token) {
    if (token == null || !token.startsWith(TOKEN_PREFIX)) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    String[] parts = token.split("_", TOKEN_PART_COUNT);
    if (parts.length != TOKEN_PART_COUNT || parts[1].isBlank() || parts[2].isBlank()) {
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

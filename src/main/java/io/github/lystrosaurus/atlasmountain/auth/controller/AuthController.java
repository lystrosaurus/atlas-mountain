package io.github.lystrosaurus.atlasmountain.auth.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.auth.service.AuthService;
import io.github.lystrosaurus.atlasmountain.auth.vo.LoginVo;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import io.github.lystrosaurus.atlasmountain.infra.ratelimit.RateLimit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @RateLimit(
      key = "'login:' + #request.username()",
      capacity = 5,
      refillPeriodSeconds = 60,
      refillTokens = 5)
  public ApiResponse<LoginVo> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.success(authService.login(request));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout() {
    authService.logout();
    return ApiResponse.success();
  }
}

package io.github.lystrosaurus.atlasmountain.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;

@RestController
@RequestMapping("/api/public")
public class PublicPingController {

  @GetMapping("/ping")
  public ApiResponse<String> ping() {
    return ApiResponse.success("public-pong");
  }
}

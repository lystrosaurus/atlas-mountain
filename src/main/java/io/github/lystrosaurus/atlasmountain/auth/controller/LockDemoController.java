package io.github.lystrosaurus.atlasmountain.auth.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import io.github.lystrosaurus.atlasmountain.infra.redis.DistributedLock;

@RestController
@RequestMapping("/api/app/locks/demo")
public class LockDemoController {

  @PostMapping("/{resourceId}")
  @DistributedLock(key = "demo:#{#resourceId}", waitTime = 1, leaseTime = 5)
  public ApiResponse<String> demo(@PathVariable String resourceId) {
    return ApiResponse.success("locked:" + resourceId);
  }
}

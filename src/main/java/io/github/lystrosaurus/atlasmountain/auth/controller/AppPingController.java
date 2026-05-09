package io.github.lystrosaurus.atlasmountain.auth.controller;

import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class AppPingController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("app-pong");
    }
}

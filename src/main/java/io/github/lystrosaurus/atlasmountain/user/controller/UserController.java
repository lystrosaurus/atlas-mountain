package io.github.lystrosaurus.atlasmountain.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.common.response.ApiResponse;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> me() {
        return ApiResponse.success(userService.getCurrentUser(StpUtil.getLoginIdAsLong()));
    }
}

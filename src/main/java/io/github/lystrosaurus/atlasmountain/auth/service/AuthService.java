package io.github.lystrosaurus.atlasmountain.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.auth.vo.LoginVo;
import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public LoginVo login(LoginRequest request) {
    UserEntity user =
        userService
            .findLoginUser(request.username())
            .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
    StpUtil.login(user.getId());
    return new LoginVo(StpUtil.getTokenName(), StpUtil.getTokenValue());
  }

  public void logout() {
    StpUtil.logout();
  }
}

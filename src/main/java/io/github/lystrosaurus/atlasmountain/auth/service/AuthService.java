package io.github.lystrosaurus.atlasmountain.auth.service;

import io.github.lystrosaurus.atlasmountain.auth.dto.LoginRequest;
import io.github.lystrosaurus.atlasmountain.auth.vo.LoginVo;

public interface AuthService {

  LoginVo login(LoginRequest request);

  void logout();
}

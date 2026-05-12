package io.github.lystrosaurus.atlasmountain.user.service;

import java.util.Optional;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;

public interface UserService {

  Optional<UserEntity> findLoginUser(String username);

  CurrentUserVo getCurrentUser(Long userId);
}

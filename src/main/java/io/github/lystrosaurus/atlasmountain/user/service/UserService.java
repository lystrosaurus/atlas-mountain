package io.github.lystrosaurus.atlasmountain.user.service;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;

import java.util.Optional;

public interface UserService {

    Optional<UserEntity> findLoginUser(String username);

    CurrentUserVo getCurrentUser(Long userId);
}

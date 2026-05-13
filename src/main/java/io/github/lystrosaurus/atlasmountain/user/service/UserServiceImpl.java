package io.github.lystrosaurus.atlasmountain.user.service;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.user.dao.UserDao;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.mapstruct.UserMapstructMapper;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserDao userDao;
  private final UserMapstructMapper userMapstructMapper;

  @Override
  public Optional<UserEntity> findLoginUser(String username) {
    return userDao
        .findByUsername(username)
        .filter(user -> UserEntity.STATUS_ENABLED.equals(user.getStatus()));
  }

  @Override
  @Cacheable(value = "users", key = "#userId")
  public CurrentUserVo getCurrentUser(Long userId) {
    UserEntity user =
        userDao
            .findById(userId)
            .filter(candidate -> UserEntity.STATUS_ENABLED.equals(candidate.getStatus()))
            .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
    return userMapstructMapper.toCurrentUserVo(user);
  }
}

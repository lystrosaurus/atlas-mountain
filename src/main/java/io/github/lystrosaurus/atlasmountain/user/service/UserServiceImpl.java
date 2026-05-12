package io.github.lystrosaurus.atlasmountain.user.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import io.github.lystrosaurus.atlasmountain.common.exception.BusinessException;
import io.github.lystrosaurus.atlasmountain.common.exception.CommonErrorCode;
import io.github.lystrosaurus.atlasmountain.user.dao.UserDao;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;

@Service
public class UserServiceImpl implements UserService {

  private final UserDao userDao;

  public UserServiceImpl(UserDao userDao) {
    this.userDao = userDao;
  }

  @Override
  public Optional<UserEntity> findLoginUser(String username) {
    return userDao.findByUsername(username).filter(user -> "ENABLED".equals(user.getStatus()));
  }

  @Override
  public CurrentUserVo getCurrentUser(Long userId) {
    UserEntity user =
        userDao
            .findById(userId)
            .filter(candidate -> "ENABLED".equals(candidate.getStatus()))
            .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED));
    return new CurrentUserVo(user.getId(), user.getUsername(), user.getNickname());
  }
}

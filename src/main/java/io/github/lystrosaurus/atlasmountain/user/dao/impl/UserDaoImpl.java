package io.github.lystrosaurus.atlasmountain.user.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.github.lystrosaurus.atlasmountain.user.dao.UserDao;
import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.mapper.UserMapper;

@Repository
public class UserDaoImpl implements UserDao {

  private final UserMapper userMapper;

  public UserDaoImpl(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @Override
  public Optional<UserEntity> findById(Long id) {
    return Optional.ofNullable(userMapper.selectById(id));
  }

  @Override
  public Optional<UserEntity> findByUsername(String username) {
    LambdaQueryWrapper<UserEntity> wrapper =
        new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username);
    return Optional.ofNullable(userMapper.selectOne(wrapper));
  }
}

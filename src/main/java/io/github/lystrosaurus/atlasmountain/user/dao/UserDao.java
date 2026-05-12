package io.github.lystrosaurus.atlasmountain.user.dao;

import java.util.Optional;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;

public interface UserDao {

  Optional<UserEntity> findById(Long id);

  Optional<UserEntity> findByUsername(String username);
}

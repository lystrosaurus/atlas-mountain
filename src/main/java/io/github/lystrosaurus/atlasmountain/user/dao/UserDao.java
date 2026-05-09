package io.github.lystrosaurus.atlasmountain.user.dao;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;

import java.util.Optional;

public interface UserDao {

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);
}

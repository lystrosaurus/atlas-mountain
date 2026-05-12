package io.github.lystrosaurus.atlasmountain.auth.dao;

import java.util.Optional;

import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;

public interface ApiTokenDao {

  Optional<ApiTokenEntity> findByPrefix(String tokenPrefix);
}

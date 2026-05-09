package io.github.lystrosaurus.atlasmountain.auth.dao;

import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;

import java.util.Optional;

public interface ApiTokenDao {

    Optional<ApiTokenEntity> findByPrefix(String tokenPrefix);
}

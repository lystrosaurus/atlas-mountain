package io.github.lystrosaurus.atlasmountain.auth.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.lystrosaurus.atlasmountain.auth.dao.ApiTokenDao;
import io.github.lystrosaurus.atlasmountain.auth.entity.ApiTokenEntity;
import io.github.lystrosaurus.atlasmountain.auth.mapper.ApiTokenMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ApiTokenDaoImpl implements ApiTokenDao {

    private final ApiTokenMapper apiTokenMapper;

    public ApiTokenDaoImpl(ApiTokenMapper apiTokenMapper) {
        this.apiTokenMapper = apiTokenMapper;
    }

    @Override
    public Optional<ApiTokenEntity> findByPrefix(String tokenPrefix) {
        LambdaQueryWrapper<ApiTokenEntity> wrapper = new LambdaQueryWrapper<ApiTokenEntity>()
                .eq(ApiTokenEntity::getTokenPrefix, tokenPrefix)
                .last("LIMIT 1");
        return Optional.ofNullable(apiTokenMapper.selectOne(wrapper));
    }
}

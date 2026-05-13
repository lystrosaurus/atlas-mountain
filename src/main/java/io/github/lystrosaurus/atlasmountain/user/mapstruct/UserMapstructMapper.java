package io.github.lystrosaurus.atlasmountain.user.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import io.github.lystrosaurus.atlasmountain.user.entity.UserEntity;
import io.github.lystrosaurus.atlasmountain.user.vo.CurrentUserVo;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapstructMapper {

  CurrentUserVo toCurrentUserVo(UserEntity entity);
}

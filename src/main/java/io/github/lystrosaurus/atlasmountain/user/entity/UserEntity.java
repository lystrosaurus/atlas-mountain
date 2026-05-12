package io.github.lystrosaurus.atlasmountain.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.github.lystrosaurus.atlasmountain.infra.persistence.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@TableName("sys_user")
@Getter
@Setter
public class UserEntity extends BaseEntity {

  public static final String STATUS_ENABLED = "ENABLED";

  @TableId private Long id;
  private String username;
  private String passwordHash;
  private String nickname;
  private String status;
}

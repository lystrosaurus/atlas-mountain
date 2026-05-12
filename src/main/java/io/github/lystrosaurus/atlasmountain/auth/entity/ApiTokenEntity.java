package io.github.lystrosaurus.atlasmountain.auth.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.github.lystrosaurus.atlasmountain.infra.persistence.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@TableName("api_token")
@Getter
@Setter
public class ApiTokenEntity extends BaseEntity {

  public static final String STATUS_ENABLED = "ENABLED";

  @TableId private Long id;
  private String name;
  private String tokenPrefix;
  private String tokenHash;
  private String status;
  private LocalDateTime expiresAt;
}

package io.github.lystrosaurus.atlasmountain.infra.persistence;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEntity {

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT)
  private Long createdBy;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Long updatedBy;

  @TableLogic private Boolean deleted;
}

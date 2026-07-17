package io.github.lystrosaurus.atlasmountain.infra.persistence;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import io.github.lystrosaurus.atlasmountain.infra.context.UserContext;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

  private static final String FIELD_CREATED_AT = "createdAt";
  private static final String FIELD_CREATED_BY = "createdBy";
  private static final String FIELD_UPDATED_AT = "updatedAt";
  private static final String FIELD_UPDATED_BY = "updatedBy";

  @Override
  public void insertFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
    Long currentUserId = UserContext.getCurrentUserId();
    strictInsertFill(metaObject, FIELD_CREATED_AT, LocalDateTime.class, now);
    strictInsertFill(metaObject, FIELD_CREATED_BY, Long.class, currentUserId);
    strictInsertFill(metaObject, FIELD_UPDATED_AT, LocalDateTime.class, now);
    strictInsertFill(metaObject, FIELD_UPDATED_BY, Long.class, currentUserId);
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
    strictUpdateFill(metaObject, FIELD_UPDATED_AT, LocalDateTime.class, now);
    strictUpdateFill(metaObject, FIELD_UPDATED_BY, Long.class, UserContext.getCurrentUserId());
  }
}

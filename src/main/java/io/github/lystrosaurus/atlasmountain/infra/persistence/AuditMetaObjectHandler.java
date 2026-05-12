package io.github.lystrosaurus.atlasmountain.infra.persistence;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

  private static final Long SYSTEM_USER_ID = 0L;
  private static final String FIELD_CREATED_AT = "createdAt";
  private static final String FIELD_CREATED_BY = "createdBy";
  private static final String FIELD_UPDATED_AT = "updatedAt";
  private static final String FIELD_UPDATED_BY = "updatedBy";

  @Override
  public void insertFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now();
    strictInsertFill(metaObject, FIELD_CREATED_AT, LocalDateTime.class, now);
    strictInsertFill(metaObject, FIELD_CREATED_BY, Long.class, SYSTEM_USER_ID);
    strictInsertFill(metaObject, FIELD_UPDATED_AT, LocalDateTime.class, now);
    strictInsertFill(metaObject, FIELD_UPDATED_BY, Long.class, SYSTEM_USER_ID);
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    strictUpdateFill(metaObject, FIELD_UPDATED_AT, LocalDateTime.class, LocalDateTime.now());
    strictUpdateFill(metaObject, FIELD_UPDATED_BY, Long.class, SYSTEM_USER_ID);
  }
}

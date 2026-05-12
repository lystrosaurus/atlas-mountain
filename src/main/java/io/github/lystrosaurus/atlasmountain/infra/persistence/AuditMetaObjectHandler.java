package io.github.lystrosaurus.atlasmountain.infra.persistence;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

  private static final Long SYSTEM_USER_ID = 0L;

  @Override
  public void insertFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now();
    strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
    strictInsertFill(metaObject, "createdBy", Long.class, SYSTEM_USER_ID);
    strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    strictInsertFill(metaObject, "updatedBy", Long.class, SYSTEM_USER_ID);
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    strictUpdateFill(metaObject, "updatedBy", Long.class, SYSTEM_USER_ID);
  }
}

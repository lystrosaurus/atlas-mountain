package io.github.lystrosaurus.atlasmountain.cdc.handler;

import io.github.lystrosaurus.atlasmountain.cdc.event.DeleteEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.InsertEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.UpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingBinlogHandler implements BinlogEventHandler {

  @Override
  public void onInsert(InsertEvent event) {
    log.info("CDC INSERT {}.{} rows={}", event.getDatabase(), event.getTable(),
        event.getRows().size());
  }

  @Override
  public void onUpdate(UpdateEvent event) {
    log.info("CDC UPDATE {}.{} rows={}", event.getDatabase(), event.getTable(),
        event.getRows().size());
  }

  @Override
  public void onDelete(DeleteEvent event) {
    log.info("CDC DELETE {}.{} rows={}", event.getDatabase(), event.getTable(),
        event.getRows().size());
  }

  @Override
  public boolean supports(String database, String table) {
    return true;
  }
}

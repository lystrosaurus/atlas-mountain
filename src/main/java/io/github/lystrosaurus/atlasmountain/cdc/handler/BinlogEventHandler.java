package io.github.lystrosaurus.atlasmountain.cdc.handler;

import io.github.lystrosaurus.atlasmountain.cdc.event.DeleteEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.InsertEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.UpdateEvent;

public interface BinlogEventHandler {
  void onInsert(InsertEvent event);
  void onUpdate(UpdateEvent event);
  void onDelete(DeleteEvent event);
  boolean supports(String database, String table);
}

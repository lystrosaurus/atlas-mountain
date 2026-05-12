package io.github.lystrosaurus.atlasmountain.cdc.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public class UpdateEvent {
  private final String database;
  private final String table;
  private final List<Map.Entry<Serializable[], Serializable[]>> rows;
  private final Instant timestamp;

  public UpdateEvent(
      String database, String table, List<Map.Entry<Serializable[], Serializable[]>> rows) {
    this.database = database;
    this.table = table;
    this.rows = rows;
    this.timestamp = Instant.now();
  }
}

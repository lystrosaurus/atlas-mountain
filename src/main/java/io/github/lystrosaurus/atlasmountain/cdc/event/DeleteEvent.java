package io.github.lystrosaurus.atlasmountain.cdc.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Getter;

@Getter
public class DeleteEvent {
  private final String database;
  private final String table;
  private final List<Serializable[]> rows;
  private final Instant timestamp;

  public DeleteEvent(String database, String table, List<Serializable[]> rows) {
    this.database = database;
    this.table = table;
    this.rows = rows;
    this.timestamp = Instant.now();
  }
}

package io.github.lystrosaurus.atlasmountain.cdc.dispatcher;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.EventHeader;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

import io.github.lystrosaurus.atlasmountain.cdc.event.DeleteEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.InsertEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.UpdateEvent;
import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinlogEventDispatcher implements BinaryLogClient.EventListener {

  private static final int TABLE_MAP_MAX_SIZE = 1000;

  private final Map<Long, TableMapEventData> tableMap =
      new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, TableMapEventData> eldest) {
          return size() >= TABLE_MAP_MAX_SIZE;
        }
      };
  private final List<BinlogEventHandler> handlers;

  public BinlogEventDispatcher(List<BinlogEventHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public void onEvent(Event event) {
    try {
      EventHeader header = event.getHeader();
      if (header == null) {
        return;
      }
      EventType eventType = header.getEventType();

      switch (eventType) {
        case TABLE_MAP -> {
          TableMapEventData data = event.getData();
          if (data != null) {
            tableMap.put(data.getTableId(), data);
          }
        }
        case WRITE_ROWS,
                EXT_WRITE_ROWS,
                UPDATE_ROWS,
                EXT_UPDATE_ROWS,
                DELETE_ROWS,
                EXT_DELETE_ROWS ->
            dispatchRowMutation(eventType, event);
        default -> {
          // ignore other event types
        }
      }
    } catch (Exception e) {
      EventType eventType = event.getHeader() != null ? event.getHeader().getEventType() : null;
      log.error("Binlog event dispatch error: eventType={}", eventType, e);
    }
  }

  private void dispatchRowMutation(EventType eventType, Event event) {
    RowMutationData mutation = new RowMutationData(event.getData());
    TableMapEventData tableMeta = tableMap.get(mutation.getTableId());
    if (tableMeta == null) {
      return;
    }
    String database = tableMeta.getDatabase();
    String table = tableMeta.getTable();

    switch (eventType) {
      case WRITE_ROWS, EXT_WRITE_ROWS -> {
        InsertEvent insertEvent = new InsertEvent(database, table, mutation.getInsertRows());
        handlers.stream()
            .filter(h -> h.supports(database, table))
            .forEach(h -> safeHandle(h, database, table, () -> h.onInsert(insertEvent)));
      }
      case UPDATE_ROWS, EXT_UPDATE_ROWS -> {
        UpdateEvent updateEvent = new UpdateEvent(database, table, mutation.getUpdateRows());
        handlers.stream()
            .filter(h -> h.supports(database, table))
            .forEach(h -> safeHandle(h, database, table, () -> h.onUpdate(updateEvent)));
      }
      case DELETE_ROWS, EXT_DELETE_ROWS -> {
        DeleteEvent deleteEvent = new DeleteEvent(database, table, mutation.getDeleteRows());
        handlers.stream()
            .filter(h -> h.supports(database, table))
            .forEach(h -> safeHandle(h, database, table, () -> h.onDelete(deleteEvent)));
      }
      default -> log.debug("Unhandled row mutation event type: {}", eventType);
    }
  }

  private void safeHandle(
      BinlogEventHandler handler, String database, String table, Runnable action) {
    try {
      action.run();
    } catch (Exception e) {
      log.error(
          "Binlog handler error: handler={}, database={}, table={}",
          handler.getClass().getSimpleName(),
          database,
          table,
          e);
    }
  }

  static class RowMutationData {
    private long tableId;
    private List<Serializable[]> insertRows;
    private List<Serializable[]> deleteRows;
    private List<Map.Entry<Serializable[], Serializable[]>> updateRows;

    RowMutationData(EventData eventData) {
      switch (eventData) {
        case UpdateRowsEventData data -> {
          this.tableId = data.getTableId();
          this.updateRows = data.getRows();
        }
        case WriteRowsEventData data -> {
          this.tableId = data.getTableId();
          this.insertRows = data.getRows();
        }
        case DeleteRowsEventData data -> {
          this.tableId = data.getTableId();
          this.deleteRows = data.getRows();
        }
        case null -> throw new IllegalArgumentException("Unsupported event data type: null");
        default ->
            throw new IllegalArgumentException(
                "Unsupported event data type: " + eventData.getClass().getName());
      }
    }

    long getTableId() {
      return tableId;
    }

    List<Serializable[]> getInsertRows() {
      return insertRows;
    }

    List<Serializable[]> getDeleteRows() {
      return deleteRows;
    }

    List<Map.Entry<Serializable[], Serializable[]>> getUpdateRows() {
      return updateRows;
    }
  }
}

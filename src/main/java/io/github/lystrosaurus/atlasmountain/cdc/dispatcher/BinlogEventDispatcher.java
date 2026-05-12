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
          return size() > TABLE_MAP_MAX_SIZE;
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
      log.error("Binlog event dispatch error", e);
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
            .forEach(h -> safeHandle(() -> h.onInsert(insertEvent)));
      }
      case UPDATE_ROWS, EXT_UPDATE_ROWS -> {
        UpdateEvent updateEvent = new UpdateEvent(database, table, mutation.getUpdateRows());
        handlers.stream()
            .filter(h -> h.supports(database, table))
            .forEach(h -> safeHandle(() -> h.onUpdate(updateEvent)));
      }
      case DELETE_ROWS, EXT_DELETE_ROWS -> {
        DeleteEvent deleteEvent = new DeleteEvent(database, table, mutation.getDeleteRows());
        handlers.stream()
            .filter(h -> h.supports(database, table))
            .forEach(h -> safeHandle(() -> h.onDelete(deleteEvent)));
      }
      default -> log.debug("Unhandled row mutation event type: {}", eventType);
    }
  }

  private void safeHandle(Runnable action) {
    try {
      action.run();
    } catch (Exception e) {
      log.error("Handler error", e);
    }
  }

  static class RowMutationData {
    private long tableId;
    private List<Serializable[]> insertRows;
    private List<Serializable[]> deleteRows;
    private List<Map.Entry<Serializable[], Serializable[]>> updateRows;

    RowMutationData(EventData eventData) {
      if (eventData instanceof UpdateRowsEventData data) {
        this.tableId = data.getTableId();
        this.updateRows = data.getRows();
      } else if (eventData instanceof WriteRowsEventData data) {
        this.tableId = data.getTableId();
        this.insertRows = data.getRows();
      } else if (eventData instanceof DeleteRowsEventData data) {
        this.tableId = data.getTableId();
        this.deleteRows = data.getRows();
      } else {
        throw new IllegalArgumentException(
            "Unsupported event data type: "
                + (eventData != null ? eventData.getClass().getName() : "null"));
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

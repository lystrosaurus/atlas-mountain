package io.github.lystrosaurus.atlasmountain.cdc.dispatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.EventHeader;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;

class BinlogEventDispatcherTest {

  @Test
  void dispatchesInsertToMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(true);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, createTableMap()));
    ArrayList<Serializable[]> rows = new ArrayList<>();
    rows.add(new Serializable[] {"alice"});
    dispatcher.onEvent(createEvent(EventType.WRITE_ROWS, createWriteRows(rows)));

    verify(handler).onInsert(any());
  }

  @Test
  void doesNotDispatchToNonMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(false);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, createTableMap()));
    dispatcher.onEvent(createEvent(EventType.WRITE_ROWS, createWriteRows(Collections.emptyList())));

    verify(handler, never()).onInsert(any());
  }

  @Test
  void dispatchesUpdateToMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(true);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, createTableMap()));
    dispatcher.onEvent(
        createEvent(EventType.UPDATE_ROWS, createUpdateRows(Collections.emptyList())));

    verify(handler).onUpdate(any());
  }

  @Test
  void dispatchesDeleteToMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(true);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, createTableMap()));
    dispatcher.onEvent(
        createEvent(EventType.DELETE_ROWS, createDeleteRows(Collections.emptyList())));

    verify(handler).onDelete(any());
  }

  private Event createEvent(EventType type, EventData data) {
    Event event = mock(Event.class);
    EventHeader header = mock(EventHeader.class);
    when(event.getHeader()).thenReturn(header);
    when(header.getEventType()).thenReturn(type);
    when(event.getData()).thenReturn(data);
    return event;
  }

  private TableMapEventData createTableMap() {
    TableMapEventData tableMap = new TableMapEventData();
    tableMap.setTableId(1L);
    tableMap.setDatabase("test_db");
    tableMap.setTable("users");
    return tableMap;
  }

  private WriteRowsEventData createWriteRows(List<Serializable[]> rows) {
    WriteRowsEventData data = new WriteRowsEventData();
    data.setTableId(1L);
    data.setRows(rows);
    return data;
  }

  private UpdateRowsEventData createUpdateRows(
      List<java.util.Map.Entry<Serializable[], Serializable[]>> rows) {
    UpdateRowsEventData data = new UpdateRowsEventData();
    data.setTableId(1L);
    data.setRows(rows);
    return data;
  }

  private DeleteRowsEventData createDeleteRows(List<Serializable[]> rows) {
    DeleteRowsEventData data = new DeleteRowsEventData();
    data.setTableId(1L);
    data.setRows(rows);
    return data;
  }
}

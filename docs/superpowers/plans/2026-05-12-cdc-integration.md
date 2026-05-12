# CDC Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate MySQL binlog CDC capability from cerberus into atlas-mountain as an independent `cdc` feature package with config, engine, dispatcher, handler, and event layers.

**Architecture:** CDC lives as a standalone feature package under `cdc/` and does not participate in the Controller->Service->DAO layered flow. It is triggered by database-level binlog events, not HTTP requests. Uses `mysql-binlog-connector-java` to connect to MySQL as a replica client and dispatches structured events to registered handlers.

**Tech Stack:** Spring Boot 4.0.6, Java 21, Lombok, mysql-binlog-connector-java 0.40.3, JUnit 5, Mockito

---

## File Map

| File | Action | Responsibility |
|---|---|---|
| `pom.xml` | Modify | Add `mysql-binlog-connector-java` dependency |
| `cdc/config/CdcProperties.java` | Create | `@ConfigurationProperties(prefix = "cdc")` for host/port/user/pass/server-id/keep-alive settings |
| `cdc/config/CdcConfig.java` | Create | Conditional bean registration when `cdc.enabled=true` |
| `cdc/event/InsertEvent.java` | Create | Immutable event carrying database, table, list of inserted rows, timestamp |
| `cdc/event/UpdateEvent.java` | Create | Immutable event carrying database, table, list of before/after row pairs, timestamp |
| `cdc/event/DeleteEvent.java` | Create | Immutable event carrying database, table, list of deleted rows, timestamp |
| `cdc/handler/BinlogEventHandler.java` | Create | Interface: `onInsert`, `onUpdate`, `onDelete`, `supports(database, table)` |
| `cdc/handler/LoggingBinlogHandler.java` | Create | Default handler that logs all events at INFO level |
| `cdc/engine/BinlogEngine.java` | Create | `Runnable` that builds `BinaryLogClient` and connects to MySQL |
| `cdc/engine/EmbeddedEngineExecutorService.java` | Create | `SmartLifecycle` that runs `BinlogEngine` in a single-thread executor |
| `cdc/dispatcher/BinlogEventDispatcher.java` | Create | `BinaryLogClient.EventListener` that parses TABLE_MAP/WRITE/UPDATE/DELETE and routes to handlers |
| `cdc/dispatcher/BinlogEventDispatcherTest.java` | Create | Unit tests for event parsing and handler routing |
| `cdc/CdcConfigTest.java` | Create | Spring Boot test verifying beans are registered when `cdc.enabled=true` |

---

### Task 1: Add dependency to pom.xml

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add mysql-binlog-connector-java dependency**

Insert after the `redisson` dependency block:

```xml
        <dependency>
            <groupId>io.debezium</groupId>
            <artifactId>mysql-binlog-connector-java</artifactId>
            <version>0.40.3</version>
        </dependency>
```

- [ ] **Step 2: Verify Maven can resolve the dependency**

Run: `mvn dependency:resolve -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "chore: add mysql-binlog-connector-java dependency

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 2: Create event objects

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/event/InsertEvent.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/event/UpdateEvent.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/event/DeleteEvent.java`

- [ ] **Step 1: Create InsertEvent**

```java
package io.github.lystrosaurus.atlasmountain.cdc.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Getter;

@Getter
public class InsertEvent {

  private final String database;
  private final String table;
  private final List<Serializable[]> rows;
  private final Instant timestamp;

  public InsertEvent(String database, String table, List<Serializable[]> rows) {
    this.database = database;
    this.table = table;
    this.rows = rows;
    this.timestamp = Instant.now();
  }
}
```

- [ ] **Step 2: Create UpdateEvent**

```java
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

  public UpdateEvent(String database, String table,
      List<Map.Entry<Serializable[], Serializable[]>> rows) {
    this.database = database;
    this.table = table;
    this.rows = rows;
    this.timestamp = Instant.now();
  }
}
```

- [ ] **Step 3: Create DeleteEvent**

```java
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
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/cdc/event/
git commit -m "feat(cdc): add InsertEvent, UpdateEvent, DeleteEvent

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 3: Create handler interface and default implementation

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/handler/BinlogEventHandler.java`
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/handler/LoggingBinlogHandler.java`

- [ ] **Step 1: Create BinlogEventHandler interface**

```java
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
```

- [ ] **Step 2: Create LoggingBinlogHandler**

```java
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
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/cdc/handler/
git commit -m "feat(cdc): add BinlogEventHandler interface and LoggingBinlogHandler

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 4: Create CdcProperties configuration class

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/config/CdcProperties.java`

- [ ] **Step 1: Create CdcProperties**

```java
package io.github.lystrosaurus.atlasmountain.cdc.config;

import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cdc")
@Data
public class CdcProperties {

  private boolean enabled = false;

  private String host = "localhost";

  private int port = 3306;

  private String username;

  private String password;

  private long serverId;

  private boolean keepAlive = true;

  private long keepAliveInterval = TimeUnit.MINUTES.toMillis(1L);

  private long connectTimeout = TimeUnit.SECONDS.toMillis(3L);

  private long heartbeatInterval = TimeUnit.SECONDS.toMillis(6L);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/cdc/config/CdcProperties.java
git commit -m "feat(cdc): add CdcProperties configuration

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 5: Create BinlogEngine

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/engine/BinlogEngine.java`

- [ ] **Step 1: Create BinlogEngine**

```java
package io.github.lystrosaurus.atlasmountain.cdc.engine;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import io.github.lystrosaurus.atlasmountain.cdc.config.CdcProperties;
import io.github.lystrosaurus.atlasmountain.cdc.dispatcher.BinlogEventDispatcher;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinlogEngine implements Runnable {

  private BinaryLogClient client;

  private final CdcProperties cdcProperties;
  private final BinlogEventDispatcher dispatcher;

  public BinlogEngine(CdcProperties cdcProperties, BinlogEventDispatcher dispatcher) {
    this.cdcProperties = cdcProperties;
    this.dispatcher = dispatcher;
  }

  @Override
  public void run() {
    log.info("Starting binlog client (serverId={}) connecting to {}:{}",
        cdcProperties.getServerId(), cdcProperties.getHost(), cdcProperties.getPort());
    client = new BinaryLogClient(
        cdcProperties.getHost(),
        cdcProperties.getPort(),
        cdcProperties.getUsername(),
        cdcProperties.getPassword());
    client.registerEventListener(dispatcher);
    client.setKeepAlive(cdcProperties.isKeepAlive());
    client.setKeepAliveInterval(cdcProperties.getKeepAliveInterval());
    client.setHeartbeatInterval(cdcProperties.getHeartbeatInterval());
    client.setConnectTimeout(cdcProperties.getConnectTimeout());
    client.setServerId(cdcProperties.getServerId());
    try {
      client.connect();
    } catch (IOException e) {
      log.error("Binlog connection failed", e);
    }
  }

  public void close() throws IOException {
    log.info("Shutting down binlog engine");
    if (client != null) {
      client.disconnect();
    }
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/cdc/engine/BinlogEngine.java
git commit -m "feat(cdc): add BinlogEngine

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 6: Create EmbeddedEngineExecutorService

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/engine/EmbeddedEngineExecutorService.java`

- [ ] **Step 1: Create EmbeddedEngineExecutorService**

```java
package io.github.lystrosaurus.atlasmountain.cdc.engine;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

@Slf4j
public class EmbeddedEngineExecutorService implements SmartLifecycle {

  private final BinlogEngine engine;
  private final ExecutorService executor;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public EmbeddedEngineExecutorService(BinlogEngine engine) {
    this.engine = engine;
    this.executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void start() {
    log.info("Starting embedded CDC engine");
    executor.execute(engine);
    running.set(true);
  }

  @Override
  public void stop() {
    log.info("Stopping embedded CDC engine");
    try {
      engine.close();
    } catch (IOException e) {
      log.warn("Failed to close binlog engine", e);
    }
    running.set(false);
    executor.shutdown();
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/cdc/engine/EmbeddedEngineExecutorService.java
git commit -m "feat(cdc): add EmbeddedEngineExecutorService lifecycle manager

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 7: Create BinlogEventDispatcher

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/dispatcher/BinlogEventDispatcher.java`

- [ ] **Step 1: Create BinlogEventDispatcher**

```java
package io.github.lystrosaurus.atlasmountain.cdc.dispatcher;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.github.lystrosaurus.atlasmountain.cdc.event.DeleteEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.InsertEvent;
import io.github.lystrosaurus.atlasmountain.cdc.event.UpdateEvent;
import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BinlogEventDispatcher implements BinaryLogClient.EventListener {

  private final Map<Long, TableMapEventData> tableMap = new HashMap<>();
  private final List<BinlogEventHandler> handlers;

  public BinlogEventDispatcher(List<BinlogEventHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public void onEvent(Event event) {
    try {
      EventHeaderV4 header = event.getHeader();
      if (header == null) {
        return;
      }
      EventType eventType = header.getEventType();

      if (eventType == EventType.TABLE_MAP) {
        TableMapEventData data = event.getData();
        if (data != null) {
          tableMap.put(data.getTableId(), data);
        }
      } else if (EventType.isRowMutation(eventType)) {
        dispatchRowMutation(eventType, event);
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

    if (EventType.isWrite(eventType)) {
      InsertEvent insertEvent = new InsertEvent(database, table, mutation.getInsertRows());
      handlers.stream().filter(h -> h.supports(database, table))
          .forEach(h -> safeHandle(() -> h.onInsert(insertEvent)));
    } else if (EventType.isUpdate(eventType)) {
      UpdateEvent updateEvent = new UpdateEvent(database, table, mutation.getUpdateRows());
      handlers.stream().filter(h -> h.supports(database, table))
          .forEach(h -> safeHandle(() -> h.onUpdate(updateEvent)));
    } else if (EventType.isDelete(eventType)) {
      DeleteEvent deleteEvent = new DeleteEvent(database, table, mutation.getDeleteRows());
      handlers.stream().filter(h -> h.supports(database, table))
          .forEach(h -> safeHandle(() -> h.onDelete(deleteEvent)));
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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/cdc/dispatcher/BinlogEventDispatcher.java
git commit -m "feat(cdc): add BinlogEventDispatcher with handler routing

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 8: Create CdcConfig

**Files:**
- Create: `src/main/java/io/github/lystrosaurus/atlasmountain/cdc/config/CdcConfig.java`

- [ ] **Step 1: Create CdcConfig**

```java
package io.github.lystrosaurus.atlasmountain.cdc.config;

import io.github.lystrosaurus.atlasmountain.cdc.dispatcher.BinlogEventDispatcher;
import io.github.lystrosaurus.atlasmountain.cdc.engine.BinlogEngine;
import io.github.lystrosaurus.atlasmountain.cdc.engine.EmbeddedEngineExecutorService;
import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CdcProperties.class)
@ConditionalOnProperty(prefix = "cdc", name = "enabled", havingValue = "true")
public class CdcConfig {

  @Bean
  public BinlogEngine binlogEngine(CdcProperties cdcProperties,
      BinlogEventDispatcher dispatcher) {
    return new BinlogEngine(cdcProperties, dispatcher);
  }

  @Bean
  public EmbeddedEngineExecutorService embeddedEngineExecutorService(BinlogEngine engine) {
    return new EmbeddedEngineExecutorService(engine);
  }

  @Bean
  public BinlogEventDispatcher binlogEventDispatcher(List<BinlogEventHandler> handlers) {
    return new BinlogEventDispatcher(handlers);
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/io/github/lystrosaurus/atlasmountain/cdc/config/CdcConfig.java
git commit -m "feat(cdc): add CdcConfig with conditional bean registration

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 9: Write BinlogEventDispatcher unit test

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/cdc/dispatcher/BinlogEventDispatcherTest.java`

- [ ] **Step 1: Write the test**

```java
package io.github.lystrosaurus.atlasmountain.cdc.dispatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.github.lystrosaurus.atlasmountain.cdc.handler.BinlogEventHandler;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class BinlogEventDispatcherTest {

  @Test
  void dispatchesInsertToMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(true);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    // First send TABLE_MAP
    TableMapEventData tableMap = new TableMapEventData();
    tableMap.setTableId(1L);
    tableMap.setDatabase("test_db");
    tableMap.setTable("users");
    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, tableMap));

    // Then send WRITE_ROWS
    WriteRowsEventData writeData = new WriteRowsEventData();
    writeData.setTableId(1L);
    writeData.setRows(List.of(new Object[]{"alice"}));
    dispatcher.onEvent(createEvent(EventType.WRITE_ROWS, writeData));

    verify(handler).onInsert(any());
  }

  @Test
  void doesNotDispatchToNonMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(false);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    TableMapEventData tableMap = new TableMapEventData();
    tableMap.setTableId(1L);
    tableMap.setDatabase("test_db");
    tableMap.setTable("users");
    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, tableMap));

    WriteRowsEventData writeData = new WriteRowsEventData();
    writeData.setTableId(1L);
    dispatcher.onEvent(createEvent(EventType.WRITE_ROWS, writeData));

    verify(handler, never()).onInsert(any());
  }

  @Test
  void dispatchesUpdateToMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(true);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    TableMapEventData tableMap = new TableMapEventData();
    tableMap.setTableId(1L);
    tableMap.setDatabase("test_db");
    tableMap.setTable("users");
    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, tableMap));

    UpdateRowsEventData updateData = new UpdateRowsEventData();
    updateData.setTableId(1L);
    updateData.setRows(Collections.emptyList());
    dispatcher.onEvent(createEvent(EventType.UPDATE_ROWS, updateData));

    verify(handler).onUpdate(any());
  }

  @Test
  void dispatchesDeleteToMatchingHandler() {
    BinlogEventHandler handler = mock(BinlogEventHandler.class);
    when(handler.supports("test_db", "users")).thenReturn(true);

    BinlogEventDispatcher dispatcher = new BinlogEventDispatcher(List.of(handler));

    TableMapEventData tableMap = new TableMapEventData();
    tableMap.setTableId(1L);
    tableMap.setDatabase("test_db");
    tableMap.setTable("users");
    dispatcher.onEvent(createEvent(EventType.TABLE_MAP, tableMap));

    DeleteRowsEventData deleteData = new DeleteRowsEventData();
    deleteData.setTableId(1L);
    dispatcher.onEvent(createEvent(EventType.DELETE_ROWS, deleteData));

    verify(handler).onDelete(any());
  }

  private Event createEvent(EventType type, Object data) {
    Event event = new Event(new EventHeaderV4(), null);
    event.getHeader().setEventType(type);
    event.setData(data);
    return event;
  }
}
```

- [ ] **Step 2: Run the test**

Run: `mvn test -Dtest=BinlogEventDispatcherTest`
Expected: Tests pass (4 tests)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/cdc/dispatcher/
git commit -m "test(cdc): add BinlogEventDispatcherTest

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 10: Write CdcConfig integration test

**Files:**
- Create: `src/test/java/io/github/lystrosaurus/atlasmountain/cdc/CdcConfigTest.java`

- [ ] **Step 1: Write the test**

```java
package io.github.lystrosaurus.atlasmountain.cdc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lystrosaurus.atlasmountain.cdc.config.CdcConfig;
import io.github.lystrosaurus.atlasmountain.cdc.engine.BinlogEngine;
import io.github.lystrosaurus.atlasmountain.cdc.engine.EmbeddedEngineExecutorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "cdc.enabled=true",
    "cdc.host=localhost",
    "cdc.port=3306",
    "cdc.username=test",
    "cdc.password=test",
    "cdc.server-id=9999"
})
class CdcConfigTest {

  @Autowired(required = false)
  private BinlogEngine binlogEngine;

  @Autowired(required = false)
  private EmbeddedEngineExecutorService executorService;

  @Test
  void beansAreRegisteredWhenCdcEnabled() {
    assertThat(binlogEngine).isNotNull();
    assertThat(executorService).isNotNull();
  }
}
```

- [ ] **Step 2: Run the test**

Run: `mvn test -Dtest=CdcConfigTest`
Expected: Test passes

- [ ] **Step 3: Commit**

```bash
git add src/test/java/io/github/lystrosaurus/atlasmountain/cdc/CdcConfigTest.java
git commit -m "test(cdc): add CdcConfig integration test

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 11: Run full test suite and ArchUnit checks

**Files:**
- None (verification only)

- [ ] **Step 1: Run all tests**

Run: `mvn test`
Expected: All tests pass including existing 35 tests + 5 new CDC tests

- [ ] **Step 2: Run Spotless check**

Run: `mvn spotless:apply`
Expected: Formats CDC code to Google Java Format

- [ ] **Step 3: Re-run tests after formatting**

Run: `mvn test`
Expected: All tests pass

- [ ] **Step 4: Commit formatting**

```bash
git add -A
git commit -m "style(cdc): apply Google Java Format

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Self-Review Checklist

- [ ] **Spec coverage:** All spec requirements (config, engine, dispatcher, handler, event, conditional registration, tests) are covered by tasks.
- [ ] **No placeholders:** Every task contains complete code, exact file paths, and exact commands.
- [ ] **Type consistency:** `CdcProperties` field names match usage in `BinlogEngine`. Handler method signatures match event types across all tasks.
- [ ] **Architecture compliance:** CDC package is independent, does not import Controller/Service/DAO/Mapper layers. `CdcConfig` uses `@ConditionalOnProperty` so beans are only created when enabled.
